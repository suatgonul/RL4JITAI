package power2dm.algorithm;

import power2dm.model.TaskDifficulty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by suat on 07-Jun-16.
 */
public class HabitGainRatio {
    private static Map<Integer, Integer> easy = new HashMap<Integer, Integer>();
    private static Map<Integer, Double> medium = new HashMap<Integer, Double>();
    private static Map<Integer, Integer> hard = new HashMap<Integer, Integer>();

    public static void initializeRatios() {
        // fill ratios for habit gain for easy tasks
        easy.put(0, 0);
        easy.put(1, 25);
        easy.put(2, 39);
        easy.put(3, 48);
        easy.put(4, 55);
        easy.put(5, 60);
        easy.put(6, 65);
        easy.put(7, 69);
        easy.put(8, 73);
        easy.put(9, 76);
        easy.put(10, 79);
        easy.put(11, 82);
        easy.put(12, 84);
        easy.put(13, 86);
        easy.put(14, 88);
        easy.put(15, 90);
        easy.put(16, 92);
        easy.put(17, 94);
        easy.put(18, 96);
        easy.put(19, 98);
        easy.put(20, 99);
        easy.put(21, 100);

        // fill ratios for habit gain for medium tasks
        medium.put(0, 0.0);
        medium.put(1, 2.0);
        medium.put(2, 3.0);
        medium.put(3, 5.0);
        medium.put(4, 7.0);
        medium.put(5, 8.0);
        medium.put(6, 9.0);
        medium.put(7, 10.0);
        medium.put(8, 11.0);
        medium.put(9, 12.0);
        medium.put(10, 13.0);
        medium.put(11, 14.0);
        medium.put(12, 15.0);
        medium.put(13, 15.5);
        medium.put(14, 16.0);
        medium.put(15, 16.8);
        medium.put(16, 17.5);
        medium.put(17, 17.8);
        medium.put(18, 18.4);
        medium.put(19, 19.0);
        medium.put(20, 19.9);
        medium.put(21, 20.8);
        medium.put(22, 21.7);
        medium.put(23, 22.6);
        medium.put(24, 23.5);
        medium.put(25, 24.0);
        medium.put(26, 24.5);
        medium.put(27, 25.0);
        medium.put(28, 25.5);
        medium.put(29, 26.0);
        medium.put(30, 26.4);
        medium.put(31, 26.8);
        medium.put(32, 27.2);
        medium.put(33, 27.4);
        medium.put(34, 28.0);
        medium.put(35, 28.4);
        medium.put(36, 28.8);
        medium.put(37, 29.2);
        medium.put(38, 29.6);
        medium.put(39, 30.0);
        medium.put(40, 30.3);
        medium.put(41, 30.6);
        medium.put(42, 30.9);
        medium.put(43, 31.2);
        medium.put(44, 31.5);
        medium.put(45, 31.7);
        medium.put(46, 31.9);
        medium.put(47, 32.1);
        medium.put(48, 32.3);
        medium.put(49, 32.5);
        medium.put(50, 32.7);
        medium.put(51, 32.9);
        medium.put(52, 33.1);
        medium.put(53, 33.3);
        medium.put(54, 33.5);
        medium.put(55, 33.7);
        medium.put(56, 33.9);
        medium.put(57, 34.1);
        medium.put(58, 34.3);
        medium.put(59, 34.4);
        medium.put(60, 34.5);
        medium.put(61, 34.6);
        medium.put(62, 34.7);
        medium.put(63, 34.8);
        medium.put(64, 34.9);
        medium.put(65, 35.0);

        // fill ratios for habit gain for hard tasks
        hard.put(0, 0);
        hard.put(1, 1);
        hard.put(2, 2);
        hard.put(3, 3);
        hard.put(4, 5);
        hard.put(5, 6);
        hard.put(6, 7);
        hard.put(7, 9);
        hard.put(8, 10);
        hard.put(9, 11);
        hard.put(10, 13);
        hard.put(11, 14);
        hard.put(12, 15);
        hard.put(13, 17);
        hard.put(14, 18);
        hard.put(15, 19);
        hard.put(16, 20);
        hard.put(17, 22);
        hard.put(18, 23);
        hard.put(19, 24);
        hard.put(20, 26);
        hard.put(21, 27);
        hard.put(22, 28);
        hard.put(23, 30);
        hard.put(24, 31);
        hard.put(25, 32);
        hard.put(26, 34);
        hard.put(27, 35);
        hard.put(28, 36);
        hard.put(29, 37);
        hard.put(30, 39);
        hard.put(31, 40);
        hard.put(32, 41);
        hard.put(33, 43);
        hard.put(34, 44);
        hard.put(35, 45);
        hard.put(36, 47);
        hard.put(37, 48);
        hard.put(38, 49);
        hard.put(39, 51);
        hard.put(40, 52);
        hard.put(41, 53);
        hard.put(42, 54);
        hard.put(43, 56);
        hard.put(44, 57);
        hard.put(45, 58);
        hard.put(46, 60);
        hard.put(47, 61);
        hard.put(48, 62);
        hard.put(49, 64);
        hard.put(50, 65);
        hard.put(51, 66);
        hard.put(52, 68);
        hard.put(53, 69);
        hard.put(54, 70);
        hard.put(55, 72);
        hard.put(56, 73);
        hard.put(57, 74);
        hard.put(58, 75);
        hard.put(59, 77);
        hard.put(60, 78);
        hard.put(61, 79);
        hard.put(62, 81);
        hard.put(63, 82);
        hard.put(64, 83);
        hard.put(65, 84);
        hard.put(66, 85);
        hard.put(67, 86);
        hard.put(68, 87);
        hard.put(69, 88);
        hard.put(70, 88);
        hard.put(71, 89);
        hard.put(72, 89);
        hard.put(73, 90);
        hard.put(74, 91);
        hard.put(75, 91);
        hard.put(76, 92);
        hard.put(77, 93);
        hard.put(78, 93);
        hard.put(79, 94);
        hard.put(80, 94);
        hard.put(81, 95);
        hard.put(82, 96);
        hard.put(83, 96);
        hard.put(84, 97);
        hard.put(85, 98);
        hard.put(86, 99);
        hard.put(87, 99);
        hard.put(88, 100);
    }

    public static int get(TaskDifficulty difficulty, int offset) {
        if (difficulty.equals(TaskDifficulty.EASY)) {
            return easy.get(offset);
        } else if (difficulty.equals(TaskDifficulty.MEDIUM)) {
            return (int) (medium.get(offset) / 35 * 100);
        } else if(difficulty.equals(TaskDifficulty.HARD)) {
            return hard.get(offset);
        }
        return 0;
    }
 }
