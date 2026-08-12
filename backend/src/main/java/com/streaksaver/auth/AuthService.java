package com.streaksaver.auth;

import com.streaksaver.config.JwtUtils;
import com.streaksaver.dto.AuthRequest;
import com.streaksaver.dto.AuthResponse;
import com.streaksaver.model.PlatformConfiguration;
import com.streaksaver.model.PlatformConnection;
import com.streaksaver.model.PlatformEnum;
import com.streaksaver.model.ProblemPool;
import com.streaksaver.model.SchedulerConfiguration;
import com.streaksaver.model.User;
import com.streaksaver.repository.PlatformConfigurationRepository;
import com.streaksaver.repository.PlatformConnectionRepository;
import com.streaksaver.repository.ProblemPoolRepository;
import com.streaksaver.repository.SchedulerConfigurationRepository;
import com.streaksaver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final Map<PlatformEnum, String> DEMO_HANDLES = Map.of(
            PlatformEnum.LEETCODE, "Fp1Dw82bqp",
            PlatformEnum.CODECHEF, "gold_dear_38",
            PlatformEnum.GEEKSFORGEEKS, "gokul9ac3"
    );

    private final UserRepository userRepository;
    private final PlatformConfigurationRepository platformConfigRepository;
    private final SchedulerConfigurationRepository schedulerConfigRepository;
    private final PlatformConnectionRepository platformConnectionRepository;
    private final ProblemPoolRepository problemPoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PlatformConfigurationRepository platformConfigRepository,
                       SchedulerConfigurationRepository schedulerConfigRepository,
                       PlatformConnectionRepository platformConnectionRepository,
                       ProblemPoolRepository problemPoolRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.platformConfigRepository = platformConfigRepository;
        this.schedulerConfigRepository = schedulerConfigRepository;
        this.platformConnectionRepository = platformConnectionRepository;
        this.problemPoolRepository = problemPoolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName() != null ? request.getFullName() : request.getEmail().split("@")[0])
                .timezone("Asia/Kolkata")
                .build();

        user = userRepository.save(user);

        List<PlatformEnum> defaultPriority = Arrays.asList(PlatformEnum.LEETCODE, PlatformEnum.CODECHEF, PlatformEnum.GEEKSFORGEEKS);
        PlatformConfiguration config = PlatformConfiguration.builder()
                .userId(user.getId())
                .priorityOrder(defaultPriority)
                .enabledPlatforms(defaultPriority)
                .autoSubmitEnabled(true)
                .build();
        platformConfigRepository.save(config);

        SchedulerConfiguration schedulerConfig = SchedulerConfiguration.builder()
                .userId(user.getId())
                .emergencyTime("23:30")
                .timezone("Asia/Kolkata")
                .enabled(true)
                .build();
        schedulerConfigRepository.save(schedulerConfig);

        for (PlatformEnum p : PlatformEnum.values()) {
            String handle = DEMO_HANDLES.getOrDefault(p, "user_" + p.name().toLowerCase());
            PlatformConnection conn = PlatformConnection.builder()
                    .userId(user.getId())
                    .platform(p)
                    .platformUsername(handle)
                    .connected(true)
                    .connectionMessage("Connected to " + p.getDisplayName() + " (" + handle + ")")
                    .build();
            platformConnectionRepository.save(conn);
        }

        seedProblemPoolForUser(user.getId());

        String token = jwtUtils.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .timezone(user.getTimezone())
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Refresh/reseed problem pool to ensure full list of Java problems is loaded
        seedProblemPoolForUser(user.getId());

        for (PlatformEnum p : PlatformEnum.values()) {
            platformConnectionRepository.findByUserIdAndPlatform(user.getId(), p).ifPresentOrElse(
                    conn -> {
                        if (!conn.isConnected() || conn.getPlatformUsername() == null || conn.getPlatformUsername().contains("demo_")) {
                            conn.setPlatformUsername(DEMO_HANDLES.get(p));
                            conn.setConnected(true);
                            platformConnectionRepository.save(conn);
                        }
                    },
                    () -> {
                        PlatformConnection conn = PlatformConnection.builder()
                                .userId(user.getId())
                                .platform(p)
                                .platformUsername(DEMO_HANDLES.get(p))
                                .connected(true)
                                .build();
                        platformConnectionRepository.save(conn);
                    }
            );
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .timezone(user.getTimezone())
                .build();
    }

    private void seedProblemPoolForUser(String userId) {
        // Clear existing to avoid duplicates on re-login
        problemPoolRepository.findByUserId(userId).forEach(problemPoolRepository::delete);

        // LeetCode Java Problems
        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.LEETCODE)
                .problemId("1")
                .problemTitle("Two Sum")
                .language("java")
                .solutionCode("""
                        class Solution {
                            public int[] twoSum(int[] nums, int target) {
                                Map<Integer, Integer> map = new HashMap<>();
                                for (int i = 0; i < nums.length; i++) {
                                    int complement = target - nums[i];
                                    if (map.containsKey(complement)) {
                                        return new int[] { map.get(complement), i };
                                    }
                                    map.put(nums[i], i);
                                }
                                return new int[]{};
                            }
                        }
                        """)
                .targetUrl("https://leetcode.com/problems/two-sum/")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.LEETCODE)
                .problemId("509")
                .problemTitle("Fibonacci Number")
                .language("java")
                .solutionCode("""
                        class Solution {
                            public int fib(int n) {
                                if (n <= 1) return n;
                                int a = 0, b = 1;
                                for (int i = 2; i <= n; i++) {
                                    int temp = a + b;
                                    a = b;
                                    b = temp;
                                }
                                return b;
                            }
                        }
                        """)
                .targetUrl("https://leetcode.com/problems/fibonacci-number/")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.LEETCODE)
                .problemId("172")
                .problemTitle("Factorial Trailing Zeroes")
                .language("java")
                .solutionCode("""
                        class Solution {
                            public int trailingZeroes(int n) {
                                int count = 0;
                                while (n > 0) {
                                    n /= 5;
                                    count += n;
                                }
                                return count;
                            }
                        }
                        """)
                .targetUrl("https://leetcode.com/problems/factorial-trailing-zeroes/")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.LEETCODE)
                .problemId("9")
                .problemTitle("Palindrome Number")
                .language("java")
                .solutionCode("""
                        class Solution {
                            public boolean isPalindrome(int x) {
                                if (x < 0) return false;
                                int rev = 0, temp = x;
                                while (temp != 0) {
                                    rev = rev * 10 + temp % 10;
                                    temp /= 10;
                                }
                                return rev == x;
                            }
                        }
                        """)
                .targetUrl("https://leetcode.com/problems/palindrome-number/")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.LEETCODE)
                .problemId("13")
                .problemTitle("Roman to Integer")
                .language("java")
                .solutionCode("""
                        class Solution {
                            public int romanToInt(String s) {
                                Map<Character, Integer> map = Map.of('I',1,'V',5,'X',10,'L',50,'C',100,'D',500,'M',1000);
                                int ans = 0, num = 0;
                                for (int i = s.length() - 1; i >= 0; i--) {
                                    int curr = map.get(s.charAt(i));
                                    if (curr < num) ans -= curr;
                                    else ans += curr;
                                    num = curr;
                                }
                                return ans;
                            }
                        }
                        """)
                .targetUrl("https://leetcode.com/problems/roman-to-integer/")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.LEETCODE)
                .problemId("20")
                .problemTitle("Valid Parentheses")
                .language("java")
                .solutionCode("""
                        class Solution {
                            public boolean isValid(String s) {
                                Stack<Character> stack = new Stack<>();
                                for (char c : s.toCharArray()) {
                                    if (c == '(') stack.push(')');
                                    else if (c == '{') stack.push('}');
                                    else if (c == '[') stack.push(']');
                                    else if (stack.isEmpty() || stack.pop() != c) return false;
                                }
                                return stack.isEmpty();
                            }
                        }
                        """)
                .targetUrl("https://leetcode.com/problems/valid-parentheses/")
                .active(true)
                .build());

        // CodeChef Java Problems
        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.CODECHEF)
                .problemId("FCTRL2")
                .problemTitle("Small Factorials")
                .language("java")
                .solutionCode("""
                        import java.util.*;
                        import java.math.*;

                        class Codechef {
                            public static void main (String[] args) {
                                Scanner sc = new Scanner(System.in);
                                if (!sc.hasNextInt()) return;
                                int t = sc.nextInt();
                                while (t-- > 0) {
                                    int n = sc.nextInt();
                                    BigInteger fact = BigInteger.ONE;
                                    for (int i = 1; i <= n; i++) {
                                        fact = fact.multiply(BigInteger.valueOf(i));
                                    }
                                    System.out.println(fact);
                                }
                            }
                        }
                        """)
                .targetUrl("https://www.codechef.com/problems/FCTRL2")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.CODECHEF)
                .problemId("FIBXOR")
                .problemTitle("Fibonacci Series Generator")
                .language("java")
                .solutionCode("""
                        import java.util.*;

                        class Codechef {
                            public static void main (String[] args) {
                                Scanner sc = new Scanner(System.in);
                                int n = sc.hasNextInt() ? sc.nextInt() : 10;
                                long a = 0, b = 1;
                                for (int i = 0; i < n; i++) {
                                    System.out.print(a + " ");
                                    long next = a + b;
                                    a = b;
                                    b = next;
                                }
                            }
                        }
                        """)
                .targetUrl("https://www.codechef.com/problems/FIBXOR")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.CODECHEF)
                .problemId("FLOW001")
                .problemTitle("Add Two Numbers")
                .language("java")
                .solutionCode("""
                        import java.util.*;

                        class Codechef {
                            public static void main(String[] args) {
                                Scanner sc = new Scanner(System.in);
                                if (!sc.hasNextInt()) return;
                                int t = sc.nextInt();
                                while (t-- > 0) {
                                    int a = sc.nextInt();
                                    int b = sc.nextInt();
                                    System.out.println(a + b);
                                }
                            }
                        }
                        """)
                .targetUrl("https://www.codechef.com/problems/FLOW001")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.CODECHEF)
                .problemId("FLOW006")
                .problemTitle("Sum of Digits")
                .language("java")
                .solutionCode("""
                        import java.util.*;

                        class Codechef {
                            public static void main(String[] args) {
                                Scanner sc = new Scanner(System.in);
                                if (!sc.hasNextInt()) return;
                                int t = sc.nextInt();
                                while (t-- > 0) {
                                    int n = sc.nextInt();
                                    int sum = 0;
                                    while (n > 0) {
                                        sum += n % 10;
                                        n /= 10;
                                    }
                                    System.out.println(sum);
                                }
                            }
                        }
                        """)
                .targetUrl("https://www.codechef.com/problems/FLOW006")
                .active(true)
                .build());

        // GeeksforGeeks Java Problems
        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.GEEKSFORGEEKS)
                .problemId("gfg_fib_01")
                .problemTitle("Fibonacci to Nth Term")
                .language("java")
                .solutionCode("""
                        class Solution {
                            int nthFibonacci(int n) {
                                int mod = 1000000007;
                                if (n <= 1) return n;
                                int a = 0, b = 1;
                                for (int i = 2; i <= n; i++) {
                                    int c = (a + b) % mod;
                                    a = b;
                                    b = c;
                                }
                                return b;
                            }
                        }
                        """)
                .targetUrl("https://www.geeksforgeeks.org/problems/nth-fibonacci-number1335/1")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.GEEKSFORGEEKS)
                .problemId("gfg_fact_01")
                .problemTitle("Factorial of a Number")
                .language("java")
                .solutionCode("""
                        class Solution {
                            static long factorial(int N) {
                                long fact = 1;
                                for (int i = 1; i <= N; i++) {
                                    fact *= i;
                                }
                                return fact;
                            }
                        }
                        """)
                .targetUrl("https://www.geeksforgeeks.org/problems/factorial5739/1")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.GEEKSFORGEEKS)
                .problemId("gfg_twosum_01")
                .problemTitle("Two Sum - Pair with Given Sum")
                .language("java")
                .solutionCode("""
                        class Solution {
                            boolean hasArrayTwoCandidates(int arr[], int n, int x) {
                                HashSet<Integer> set = new HashSet<>();
                                for (int i = 0; i < n; i++) {
                                    int temp = x - arr[i];
                                    if (set.contains(temp)) return true;
                                    set.add(arr[i]);
                                }
                                return false;
                            }
                        }
                        """)
                .targetUrl("https://www.geeksforgeeks.org/problems/key-pair5616/1")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.GEEKSFORGEEKS)
                .problemId("gfg_pal_01")
                .problemTitle("Palindrome String")
                .language("java")
                .solutionCode("""
                        class Solution {
                            int isPalindrome(String S) {
                                int i = 0, j = S.length() - 1;
                                while (i < j) {
                                    if (S.charAt(i) != S.charAt(j)) return 0;
                                    i++; j--;
                                }
                                return 1;
                            }
                        }
                        """)
                .targetUrl("https://www.geeksforgeeks.org/problems/palindrome-string0817/1")
                .active(true)
                .build());

        problemPoolRepository.save(ProblemPool.builder()
                .userId(userId)
                .platform(PlatformEnum.GEEKSFORGEEKS)
                .problemId("gfg_sum_01")
                .problemTitle("Sum of Array Elements")
                .language("java")
                .solutionCode("""
                        class Solution {
                            int sum(int arr[], int n) {
                                int s = 0;
                                for (int i = 0; i < n; i++) s += arr[i];
                                return s;
                            }
                        }
                        """)
                .targetUrl("https://www.geeksforgeeks.org/problems/sum-of-array-elements2502/1")
                .active(true)
                .build());
    }
}
