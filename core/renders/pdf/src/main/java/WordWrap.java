import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by marian on 4/6/17.
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
