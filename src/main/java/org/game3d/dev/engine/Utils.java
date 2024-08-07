package org.game3d.dev.engine;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Utils {
    public static String readFile(String filePath) {
        String str;
        try {
            str = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("Error reading file [" + filePath + "]", e);
        }
        return str;
    }

    public static float @NotNull [] mapFloatListToArray(@NotNull List<Float> floats) {
        float[] floatArr = new float[floats.size()];
        for (int i = 0; i < floats.size(); i++) {
            floatArr[i] = floats.get(i);
        }
        return floatArr;
    }

    public static int[] mapIntListToArray(@NotNull List<Integer> integers) {
        return integers.stream().mapToInt(Integer::intValue).toArray();
    }
}
