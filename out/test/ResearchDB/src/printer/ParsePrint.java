package printer;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.text.Font;

import java.awt.*;
import java.util.ArrayList;

public class ParsePrint {

    private double pageWidth;
    private double pageHeight;
    String[] docPages;

    ParsePrint(String text){
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        PageLayout pgLayout = printerJob.getJobSettings().getPageLayout();
        pageHeight = pgLayout.getPrintableHeight();
        pageWidth = pgLayout.getPrintableWidth();
        parseText(text);
    }

    private void parseText(String text){
        Font font = new Font("System",12);
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);

        double lineHeight = (double)metrics.getLineHeight();
        double linesPerPage = pageHeight / lineHeight;

        String[] arr = text.split("\n");
        ArrayList<String> pages = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int l = 0;
        int p = 0;

        for (String line : arr) {
            if (metrics.computeStringWidth(line) <= pageWidth) {
                sb.append(line).append("\n");
                l++;

            }else{
                int iChr = 0;
                char[] chars = line.toCharArray();
                StringBuilder temp = new StringBuilder();
                temp.setLength(0);
                for(char c : chars){
                    iChr++;
                    if(metrics.computeStringWidth(temp.toString() + c) > pageWidth) {
                        sb.append(temp.toString());
                        temp.setLength(0);
                        l++;
                        if(l == (int)linesPerPage){

                            String lastWord = lastWord(sb.toString(), Character.toString(c));
                            if (!lastWord.equals(Character.toString(c))) {
                                sb.replace(sb.lastIndexOf(lastWord), (sb.length()), "");
                            }
                            pages.add(p, sb.toString());
                            sb.setLength(0);
                            temp.setLength(0);
                            l = 0;
                            p++;
                            if (!lastWord.equals(Character.toString(c))) temp.append(lastWord);
                        }
                    }

                    temp.append(c);
                    if(iChr == chars.length) {
                        sb.append(temp.toString()).append("\n");
                        temp.setLength(0);
                        l++;
                    }
                }
            }

            if(l == (int)linesPerPage){
                pages.add(p, sb.toString());
                sb.setLength(0);
                l = 0;
                p++;
            }
        }
        pages.add(p, sb.toString());
        docPages = new String[pages.size()];
        int i = 0;
        for(String pg : pages)
            docPages[i++] = pg;
    }

    private String lastWord(String line, String chr){
        int len = line.length();
        if(chr.matches("[a-zA-Z0-9]") && line.substring(len - 1).matches("[a-zA-Z0-9]")){
            for(int p = (len -1); p >= 0; p--){
                char c = line.charAt(p);
                if(Character.toString(c).matches("[$&+,:;=\\\\?@#|/'<>.^* ()%!-]")){
                    return line.substring(p+1);
                }
            }
        }
        return chr;
    }
}
