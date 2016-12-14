package www.codeevery.com;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Created by ËÎ³¬ on 2015/10/5.
 */
public class MyText extends JTextField {
    String hint;
    public MyText(String hint){
        this.hint = hint;
        this.setText(hint);
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if(MyText.this.getText().equals(hint)){
                    MyText.this.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if(MyText.this.getText().equals("")){
                    MyText.this.setText(hint);
                }
            }
        });
    }
    public String getString(){
        if(this.getText().equals(hint)){
            return "";
        }else {
            return this.getText();
        }
    }
}
