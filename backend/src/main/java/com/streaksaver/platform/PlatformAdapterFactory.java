package com.streaksaver.platform;

import com.streaksaver.model.PlatformEnum;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PlatformAdapterFactory {
    private final Map<PlatformEnum, CodingPlatformAdapter> adapterMap = new EnumMap<>(PlatformEnum.class);

    public PlatformAdapterFactory(List<CodingPlatformAdapter> adapters) {
        for (CodingPlatformAdapter adapter : adapters) {
            adapterMap.put(adapter.getPlatform(), adapter);
        }
    }

    public CodingPlatformAdapter getAdapter(PlatformEnum platform) {
        CodingPlatformAdapter adapter = adapterMap.get(platform);
        if (adapter == null) {
            throw new IllegalArgumentException("Unsupported platform adapter: " + platform);
        }
        return adapter;
    }
}
