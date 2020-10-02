package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import model.User;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.*;
import java.util.Map;

public class Pdf {
    public void addPdf(User user, String type, String packageName) throws IOException, DocumentException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("templates/" + type)));
        //создание текстового документа
        //кладем все данные туда, а потом конвертация в pdf
        String helpFileName = RandomStringUtils.random(5, true, true) + ".txt";
        PrintWriter pw = new PrintWriter(new FileWriter("created/txt/" + helpFileName));
        while (br.ready()) {
            String line = br.readLine();
            Map<String, Object> map = user.getParameters();
            for (Map.Entry<String, Object> e :
                    map.entrySet()) {
                if (e.getValue() != null) {
                    line = line.replaceAll("\\{" + e.getKey() + "}", e.getValue().toString());
                }
            }
            pw.println(line);
        }
        br.close();
        pw.close();
        //здесь должна быть запись в пдф
        //создание папки с pdf файлом
        String folderName = "created/" + packageName;
        String allFileName = folderName + "/" + RandomStringUtils.random(5, true, true) + ".pdf";
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }
        createPdf(allFileName, helpFileName);
    }

    public void createPdf(String allFileName, String helpFileName) throws IOException, DocumentException {
        //создание pdf документа


        Document document = new Document();

        PdfWriter.getInstance(document, new FileOutputStream(allFileName));

        document.open();
        //подключаем файл шрифта, который поддерживает кириллицу
        BaseFont bf = BaseFont.createFont("font/19925.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        Font font = new Font(bf);

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("created/txt/" + helpFileName)));
        //запись в pdf текстовый файл
//        Chunk chunk = new Chunk("", font);
        Paragraph paragraph = new Paragraph();
        paragraph.setFont(font);
        paragraph.setLeading(15);
        while (br.ready()) {
            String str = br.readLine() + "\n";
            //Adding text in the form of string
            System.out.println(str);
            paragraph.add(str);
        }
        document.add(paragraph);

        document.close();
        System.out.println("закрылся");
        br.close();
    }
}
