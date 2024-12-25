package ict.minesunshineone.uid.util;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.bukkit.configuration.file.FileConfiguration;

import ict.minesunshineone.uid.UIDPlugin;
import ict.minesunshineone.uid.exception.UIDGenerationException;

public class UIDGenerator {

    private final SecureRandom random;
    private final Set<Long> usedRandomUIDs;
    private final AtomicLong sequentialCounter;
    private final int randomAllocationCount;
    private final int minRandomUID;
    private final int maxRandomUID;
    private final int sequentialStart;
    private final int formatDigits;
    private final char padChar;

    public UIDGenerator(UIDPlugin plugin) {
        this.random = new SecureRandom();
        this.usedRandomUIDs = new HashSet<>();

        FileConfiguration config = plugin.getConfig();
        this.randomAllocationCount = config.getInt("uid.random-allocation-count", 100);
        this.minRandomUID = config.getInt("uid.random-min", 1);
        this.maxRandomUID = config.getInt("uid.random-max", 100);
        this.sequentialStart = config.getInt("uid.sequential-start", 10000);

        // 验证配置
        if (minRandomUID < 1 || maxRandomUID > 9999) {
            throw new IllegalArgumentException("随机UID范围必须在1到9999之间");
        }
        if (maxRandomUID <= minRandomUID) {
            throw new IllegalArgumentException("最大随机UID必须大于最小随机UID");
        }
        if (sequentialStart <= maxRandomUID) {
            throw new IllegalArgumentException("顺序UID起始值必须大于最大随机UID");
        }

        this.formatDigits = config.getInt("uid.format.digits", 4);
        String padCharStr = config.getString("uid.format.pad-char", "0");
        this.padChar = padCharStr != null ? padCharStr.charAt(0) : '0';

        this.sequentialCounter = new AtomicLong(sequentialStart);
    }

    public String formatUID(long uid) {
        return String.format("%" + padChar + formatDigits + "d", uid);
    }

    public boolean isRandomUID(long uid) {
        return uid >= minRandomUID && uid <= maxRandomUID;
    }

    public synchronized long generateUID(int playerCount) throws UIDGenerationException {
        if (playerCount < randomAllocationCount) {
            return generateRandomUID();
        } else {
            return generateSequentialUID();
        }
    }

    private long generateRandomUID() throws UIDGenerationException {
        int attempts = 0;
        int maxAttempts = 100;

        while (attempts < maxAttempts) {
            long uid = minRandomUID + random.nextInt(maxRandomUID - minRandomUID + 1);
            if (!usedRandomUIDs.contains(uid)) {
                usedRandomUIDs.add(uid);
                return uid;
            }
            attempts++;
        }

        throw new UIDGenerationException("无法生成唯一的随机UID已尝试" + maxAttempts + "次");
    }

    private long generateSequentialUID() {
        return sequentialCounter.getAndIncrement();
    }
}
