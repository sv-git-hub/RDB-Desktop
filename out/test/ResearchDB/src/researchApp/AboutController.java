package researchApp;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import logging.LoggerWrapper;


import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable {

    @FXML private TextArea txaAbout;
    @FXML private Label lblVersion;
    @FXML private Button btnManual;

    public void initialize(URL url, ResourceBundle rb){
        lblVersion.setText("Version: " + GlobalVariables.VERSION);
        String txtBody =
            "\n  Originally, created as an apologetics database, the purpose of ResearchDB is simply to help people to " +
            "document important credible sources which support their beliefs or interest – and maybe even " +
            "important research. You can simply copy and paste in links, comments, important information and " +
            "sources which bring credibility to you and your information. Wouldn't it be nice for once if you could " +
                    "simple say, \"Sure, I got it right here. Do you want to listen?\" Now you can!\n" +
            " We live in a 'where is your evidence' culture, and evidence is king. It should be. If you cannot provide some immediate source to " +
            "why you know something to be true, it is discounted, even if you recall correctly.\n\n" +
            "  ResearchDB will help you capture the important information as a simple note to make your sources credible. Then when you are " +
            "out having a conversation and a topic you know comes up, you will have what you need. You won't need to reread or bend your mind " +
            "trying to recall what you learned, it will be at your fingers.";

/*        "\n  Originally, created as an apologetics database, the purpose of ResearchDB is simply to help people to " +
            "document important credible sources which support their beliefs or interest – and maybe even " +
            "important research. You can simply copy and paste in links, comments, important information and " +
            "sources which bring credibility to you and your information. A conversation like below is no longer " +
            "a struggle trying to recall facts about the point you are making.  Have you ever heard or had a " +
            "similar conversation to this:\n\n" +
                "\tFriend:\tI just watched a New Testament Bible Scholar on the news explaining all the corruption in the Bible.\n" +
                "\tYou:\t\tI've heard them, and their arguments are impressive but weak. I know the Bible is as accurate today as\n" +
                    "\t\t\tit was 2000 years ago.\n" +
                "\tFriend:\tThat's impossible! It was copied and copied and corrupted by those with an agenda. It's all hersey!\n" +
                    "\t\t\tBesides, this scholar said there are well over 100,000 differences in all the manuscripts and no two\n" +
                    "\t\t\tare identical.\n" +
                "\tYou:\t\tI listened to a seminar on Bible corruption addressing that and other arguments perpetrated by other\n" +
                    "\t\t\tscholars, skeptics and cynics. BTW, it is more like 500,000 differences and is easily explained away.\n" +
                    "\t\t\tWe can trust it to be 99.8% accurate in interpretation and 100% in meaning and doctrine.\n" +
                "\tFriend:\tI don't believe that at all. How can you have 500,000 differences and be that accurate.\n" +
                "\tYou:\t\tWell, I listened to it about a year ago but I can't remember exactly where. I will have try to find it.\n\n" +
            "  Wouldn't it be nice for once if you could simple say, \"Sure, I got it right here. Do you want to listen?\" Now you can! " +
            "We live in a 'where is your evidence' culture, and evidence is king. It should be. If you cannot provide some immediate source to " +
            "why you know something to be true, it is discounted, even if you recall correctly.\n\n" +
            "  ResearchDB will help you capture the important information as a simple note to make your sources credible. Then when you are " +
            "out having a conversation and a topic you know comes up, you will have what you need. You won't need to reread or bend your mind " +
            "trying to recall what you learned, it will be at your fingers.";
            */

        txaAbout.setText(txtBody);
        txaAbout.setWrapText(true);
    }

    @FXML   // Call by OnAction event for menuManual (ResearchDB Manual)
    private void openManual(){
        if(Desktop.isDesktopSupported()){
            try{
                Desktop.getDesktop().open(new File(GlobalVariables.RESEARCH_DB_PATH + "app_info/ResearchDBManual.pdf"));
                close();
            }catch(IOException ex){
                LoggerWrapper.getInstance().myLogger.severe("Launching manual failed.");
                JOptionPane.showMessageDialog(null, "Launching ResearchDB manual (PDF) failed.", "Error openManual", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void close(){
        Stage stage = (Stage) btnManual.getScene().getWindow();
        stage.close();
    }
}
