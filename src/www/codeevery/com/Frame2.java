package www.codeevery.com;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 宋超 on 2015/10/4.
 */
public class Frame2 extends JFrame {
    MyText textPath;
    JTextArea sqlText;
    MyText dbPathJ;
    MyText dbNameJ;
    MyText dbPasswordJ;
    Connection connection;
    Statement statement;

    public Frame2() {

        //文件路径文本框
        textPath = new MyText("读取的文件路径");
        //路径文本框的大小设置
        Dimension dimension = new Dimension(10000, 30);
        textPath.setMaximumSize(dimension);
        //点击读取文件按钮
        JButton buttonReadStart = new JButton("读取数据");
        buttonReadStart.addMouseListener(new readStartFromFile());

        JButton buttonHelp = new JButton("帮助");
        buttonHelp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String help = "帮助:\n填入文件路径数据库路径例如:localhost:3306/***和用户名密码"
                        +"下方的大块空白填的是sql语句，每一条语句占一行，回车换行\n"
                        +"如果读取的文件有2块数据比如用&&分隔的帖子标题数据和用户名数据\n"
                        +"要把帖子和用户插入表里语句是:insert into user values ($1[],$[]);\n"
                        +"其中的$1[]的数字部分代表第几块数据，中括号里的数字代表数据的第几条，不填就是从第一条循环插入到最后一条\n"
                        +"一定要有$符号在每个1[2]前面";
                JOptionPane.showMessageDialog(Frame2.this,help);
            }
        });

        dbPathJ = new MyText("数据库url+端口号");
        dbNameJ = new MyText("数据库用户名");
        dbPasswordJ = new MyText("数据库密码");

        //第二部分的块
        JPanel panel = new JPanel();
        panel.setMaximumSize(dimension);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(buttonReadStart);
        panel.add(buttonHelp);
        panel.add(dbPathJ);
        panel.add(dbNameJ);
        panel.add(dbPasswordJ);

        //sql文本框
        sqlText = new JTextArea();

        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.add(textPath);
        this.add(panel);
        this.add(sqlText);
        this.setTitle("爬虫数据写入数据库");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setBounds(200, 200, 1000, 500);
        this.setVisible(true);
    }

    private class readStartFromFile extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            //在这里读入写进文件的数据
            if(textPath.getText().equals("")){
                JOptionPane.showMessageDialog(Frame2.this,"文件路径不能为空");
                return;
            }
            if((connection = getStatement())==null){
                return;
            }else {
                try {
                    statement = connection.createStatement();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(Frame2.this,"获取statement出错");
                }
            }
            File file = new File(textPath.getText());
            //定义一个arraylist用来存放多个arraylist
            ArrayList<ArrayList<String>> mainList = new ArrayList<>();
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                //开始读取
                String temp;
                ArrayList<String> list = new ArrayList<>();
                while ((temp = bufferedReader.readLine()) != null) {
                    if (temp.equals("&&")) {//如果到&&符号除说明要开始下一部分
                        mainList.add(list);
                        list = new ArrayList<>();
                    } else {
                        list.add(temp);
                    }
                }
                fileReader.close();
                bufferedReader.close();
                JOptionPane.showMessageDialog(Frame2.this, "读取数据成功，请填入并执行sql");
                //测试把数据打出来看看

            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(Frame2.this, "文件路径有误");
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(Frame2.this, "文件读取错误");
                return;
            }
            //下面执行sql语句，sql语句在文本框内填写
            //读取文本框填写的文本并进行处理
            String allSql = sqlText.getText();
            String a[] = allSql.split("\n");
            //a数组的每一部分都是一条sql语句
            for (int i = 0; i < a.length; i++) {
                String sql = a[i];
                //判断这个sql语句中是否包含需要循环执行的部分
                //拆分sql语句
                Pattern pattern = Pattern.compile("(\\$[0-9]*?\\[[0-9]*?\\])");
                Matcher matcher = pattern.matcher(sql);
                ArrayList<String> groupList = new ArrayList<>();
                ArrayList<String> groupIDList = new ArrayList<>();
                ArrayList<String> groupInList = new ArrayList<>();

                int sign = -1;
                while (matcher.find()) {//循环读出所有适配分组
                    String temp = matcher.group(1);
                    groupList.add(temp);
                    //读出所有适配分组的id值和内部值
                    //判断中括号[]里面是否有数字等
                    Pattern pattern1 = Pattern.compile("\\$([0-9]*?)\\[([0-9]*?)\\]");
                    Matcher matcher1 = pattern1.matcher(temp);
                    if (matcher1.find()) {
                        groupIDList.add(matcher1.group(1));
                        groupInList.add(matcher1.group(2));
                        if(matcher1.group(2).equals("")){
                            sign = groupIDList.size()-1;
                        }
                    }
                }
                //下面往数据库里执行语句
                if(sign!=-1){//说明需要进行大量循环
                    int te = Integer.valueOf(groupIDList.get(sign));
                    if(te>0)//如果大于0那么索引减一，索引的是从文件读取的$$分隔的部分
                        te--;
                    int id = mainList.get(te).size();
                    for(int h=0;h<id;h++){
                        String tempSql = sql;
                        for(int c = 0;c<groupList.size();c++){//把内部数据读出来进行替换
                            if(groupInList.get(c).equals("")){
                                tempSql = tempSql.replace(groupList.get(c),mainList.get(Integer.valueOf(groupIDList.get(c))-1<=0?0:Integer.valueOf(groupIDList.get(c))-1).get(h));
                            }else{
                                tempSql = tempSql.replace(groupList.get(c),mainList.get(Integer.valueOf(groupIDList.get(c))-1<=0?0:Integer.valueOf(groupIDList.get(c))-1).get(Integer.valueOf(groupInList.get(c))));
                            }
                        }
                        //替换之后执行sql语句
                        if(!DB(tempSql)){
                            return;
                        }
                    }
                    JOptionPane.showMessageDialog(Frame2.this,"执行成功");
                }else{
                    for(int c = 0;c<groupList.size();c++){//把内部数据读出来进行替换
                        sql = sql.replace(groupList.get(c),mainList.get(Integer.valueOf(groupIDList.get(c))-1<=0?0:Integer.valueOf(groupIDList.get(c))-1).get(Integer.valueOf(groupInList.get(c))));
                    }
                    //执行sql语句
                    if(!DB(sql)){
                        return;
                    }
                }
            }
            try {
                JOptionPane.showMessageDialog(Frame2.this,"执行成功");
                statement.close();
                connection.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }
    private Connection getStatement(){
        String url = dbPathJ.getString();
        url = checkDBURl(url);
        String name = dbNameJ.getString();
        String password = dbPasswordJ.getString();
        try {
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
            Connection connection = DriverManager.getConnection(url,name,password);
            return connection;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(Frame2.this,"连接数据库出错");
        return null;
    }
    private String checkDBURl(String url){
        if(!url.contains("character")){
            url = url+"?useUnicode=true&characterEncoding=utf-8";
        }
        if(url.contains("jdbc:mysql://")){
            return url;
        }else{
            return "jdbc:mysql://"+url;
        }
    }
    private boolean DB(String sql){
        try {
            statement.execute(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(Frame2.this,"执行sql语句出错，sql是:\r\n"+sql);
            return false;
        }
    }
}
