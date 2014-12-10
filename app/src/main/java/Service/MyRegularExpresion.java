package Service;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by potter on 14-12-10.
 */
public class MyRegularExpresion {
    public static String regexHtml(String html, String regex) {
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(html);

        ArrayList<String> listInfo = new ArrayList<String>();

        while(matcher.find()) {
            //System.out.println("In regexHtml: " + matcher.group());
            // listInfo.add(matcher.group());
            return matcher.group();
        }

        return null;
    }
}
