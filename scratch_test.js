const { chromium } = require('playwright');
(async () => {
    const browser = await chromium.launch({ headless: true });
    const page = await browser.newPage();
    console.log("Navigating to GFG profile...");
    await page.goto("https://www.geeksforgeeks.org/user/gokul9ac3/");
    const content = await page.content();
    const match = content.match(/"total_problems_solved":\s*(\d+)/);
    console.log("GFG Total Solved Match:", match ? match[1] : "None");

    console.log("Navigating to CodeChef profile...");
    await page.goto("https://www.codechef.com/users/gold_dear_38");
    const ccContent = await page.content();
    const ccMatch = ccContent.match(/Fully Solved\s*\(\s*(\d+)\s*\)/i);
    console.log("CodeChef Solved Match:", ccMatch ? ccMatch[1] : "None");
    await browser.close();
})();
