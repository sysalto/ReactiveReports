import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
  *
 * Unless you have purchased a commercial license agreement from SysAlto
 * the following license terms apply:
 *
 * This program is part of ReactiveReports.
 *
 * ReactiveReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReactiveReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY. Without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ReactiveReports.
 * If not, see https://www.gnu.org/licenses/lgpl-3.0.en.html.
 */



public class WordWrap {
    public static void print(List<String> arr, int length) {
        ArrayList<String> curLine = new ArrayList<String>();
        int cur = 0;
        for (int i = 0; i < arr.size(); i++) {
            if (cur + arr.get(i).length() <= length) {
                curLine.add(arr.get(i));
                cur += arr.get(i).length() + 1;
            } else {
                int remainSpace = length - cur + 1;
                int spacePerword = curLine.size() == 1 ? 0 : remainSpace / (curLine.size() - 1) + 1;
                int oneMoreSpace = curLine.size() == 1 ? 0 : remainSpace % (curLine.size() - 1);
                for (int j = 0; j < curLine.size(); j++) {
                    System.out.print(curLine.get(j));
                    for (int sp = 0; sp < spacePerword; sp++) {
                        System.out.print(" ");
                    }
                    if (oneMoreSpace > 0) {
                        oneMoreSpace--;
                        System.out.print(" ");
                    }
                }
                System.out.println("");
                curLine.clear();
                curLine.add(arr.get(i));
                cur = arr.get(i).length() + 1;
            }
        }
        if (cur != 0) {
            int remainSpace = length - cur + 1;
            int spacePerword = curLine.size() == 1 ? 0 : remainSpace / (curLine.size() - 1) + 1;
            int oneMoreSpace = curLine.size() == 1 ? 0 : remainSpace % (curLine.size() - 1);
            for (int j = 0; j < curLine.size(); j++) {
                System.out.print(curLine.get(j));
                for (int sp = 0; sp < spacePerword; sp++) {
                    System.out.print(" ");
                }
                if (oneMoreSpace > 0) {
                    oneMoreSpace--;
                    System.out.print(" ");
                }
            }
            System.out.println("");
        }

    }

    public static void main(String [ ] args) {
        String a[] = new String[]{"catelus","cu","par","cret","fur","rata","din","cot"};
        List<String> list1 = Arrays.asList(a);
        print(list1,18);
    }


}
