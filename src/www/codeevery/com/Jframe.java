package www.codeevery.com;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 宋超 on 2015/10/1.
 */

public class Jframe extends JFrame {
    Container container;
    JScrollPane scrollPane;
    JPanel panel;
    MyText textNextSite;
    MyText textSite;
    public Jframe(){
        container = getContentPane();
        this.setTitle("小丁丁爬虫");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 500);
        this.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JButton button = new JButton("添加正则表达式");
        button.addMouseListener(new addTextEditListener());

        JButton button1 = new JButton("开始");
        JButton buttonHelp = new JButton("使用说明");
        buttonHelp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String help =
                        "索引部分:\r\n正则表达式的本身代表索引0，第一个括号代表索引1\r\n所有的dom匹配只能有一个索引是-1即批量匹配的，如div[class=main]索引为-1即可，如果索引为大于-1的数字，那么只能搜索单独某一个匹配部分\r\n"
                                + "htmldom部分:\r\nhtml匹配是从前向后一直向后，正则表达式和htmldom都使用那么是先搜索htmldom再使用正则表达式匹配搜索出来的每一个部分，也可以单独使用某一个功能一个搜索\r\n"
                                + "单独使用搜索htmldom会直接搜索出规定的元素的内部text\r\n"
                                + "具体教程看jsoup的select部分，htmldom搜索部分的表达式就是select的参数，如：搜索div而且class是main填入，div[class=main],即可"
                                + "正则表达式请自己学习";
                JOptionPane.showMessageDialog(panel, help);
            }
        });

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.add(button);
        jPanel.add(button1);
        jPanel.add(buttonHelp);

        Dimension dimension = new Dimension(10000,35);

        textSite = new MyText("在这里填入网址http://www~或者是html文件路径");
        textSite.setMaximumSize(dimension);

        textNextSite = new MyText("这里填入txt文件保存路径，默认D盘根目录");
        textNextSite.setMaximumSize(dimension);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(panel);

        container.add(textSite);
        container.add(textNextSite);
        container.add(scrollPane);
        container.add(jPanel);
        this.setVisible(true);

        button1.addMouseListener(new startListener());
    }
    private class addTextEditListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);

            MyText textField = new MyText("这里是正则表达式");
            Dimension dimension = new Dimension(10000,70);
            MyText textFieldN = new MyText("索引");

            JPanel panelUp = new JPanel();
            panelUp.setLayout(new BorderLayout());
            panelUp.add(textField,"Center");
            panelUp.add(textFieldN,"East");

            MyText textHtml = new MyText("这里填入html解析代码");
            MyText textN = new MyText("索引");//索引输入框
            JPanel jPanel3 = new JPanel();
            jPanel3.setLayout(new BorderLayout());
            jPanel3.add(textHtml,"Center");
            jPanel3.add(textN,"East");

            JButton htmlButton = new JButton("添加dom");
            //这个是存放多个htmldom解析文本框和添加按钮的panel
            JPanel htmlPanel = new JPanel();
            htmlPanel.setLayout(new BoxLayout(htmlPanel, BoxLayout.X_AXIS));
            htmlPanel.add(jPanel3);
            htmlPanel.add(htmlButton);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BorderLayout());
            textPanel.add(panelUp, "North");
            textPanel.add(htmlPanel, "South");

            htmlButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    JPanel jPanelC = new JPanel();
                    jPanelC.setLayout(new BorderLayout());
                    MyText myTextt = new MyText("html解析");
                    MyText myText2 = new MyText("索引");
                    jPanelC.add(myTextt,"Center");
                    jPanelC.add(myText2,"East");
                    htmlPanel.add(jPanelC, htmlPanel.getComponentCount() - 1);
                    htmlPanel.revalidate();
                }
            });

            JButton button = new JButton("删除");
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    e.getComponent().getParent().getParent().remove(e.getComponent().getParent());
                    panel.repaint();
                    panel.revalidate();
                }
            });

            JPanel panel1 = new JPanel();
            panel1.setMaximumSize(dimension);
            panel1.setLayout(new BorderLayout());
            panel1.add(textPanel, "Center");
            panel1.add(button,"East");
            panel1.setBorder(new EmptyBorder(20,0,0,0));

            panel.add(panel1);
            panel.revalidate();
        }
    }
    public class startListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            String path = textNextSite.getString();
            String site = textSite.getString();
            File file;
            if(path.equals("")) {
                file = new File("D://a.txt");
            }else{
                file = new File(path);
            }
            /*
            测试数据
             */
            //site = "http://www.douban.com/group/explore/culture";
            //-------------------------------
            FileWriter write = null;
            Document document = null;
            try {
                write = new FileWriter(file);
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(panel, "文件路径有误,文件可以不存在，但文件夹必须存在");
                e1.printStackTrace();
                return;
            }
            //下面需要获取网页数据
            if(!site.contains("http")&&site.contains(":")){//说明是读取文件
                File file1 = new File(site);
                try {
                    document = Jsoup.parse(file1,"utf-8");
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(panel,"文件路径有误或者是其他错误");
                    return;
                } catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "ioException其他错误");
                }
            }else if(site.contains("http://")){//说明是读取网页
                try {
                    URL url = new URL(site);
                    document = Jsoup.parse(url,3000);
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(panel,"网络有问题或者其他异常");
                    return;
                }
            }

            //在这里对document进行处理
            try {
                int size = panel.getComponentCount();
                JPanel panelB ;
                MyText rexIndexT;
                MyText myTextRex;
                //下面开始解析循环
                for (int i = 0; i < size; i++) {//这里把读到的所有的panel每一块在这里进行循环，每一次遍历都会遍历整个html
                    //下面遍历组件获取是每个文本框的值
                    ArrayList<String> htmlDom = new ArrayList<>();
                    ArrayList<Integer> indexList = new ArrayList<>();

                    JPanel panelA = (JPanel) panel.getComponent(i);//得到第一个panel
                    panelB = (JPanel) panelA.getComponent(0);//得到文本框panel

                    JPanel panelRex = (JPanel) panelB.getComponent(0);//得到正则表达式的panel
                    myTextRex = (MyText) panelRex.getComponent(0);//得到正则表达式文本
                    rexIndexT = (MyText) panelRex.getComponent(1);//得到正则表达式索引

                    int rexIndexText = rexIndexT.getString().equals("")?0:Integer.valueOf(rexIndexT.getString());//获取第一块的正则表达式文本
                    String rexText = myTextRex.getString();//获得第一块的正则表达式索引

                    JPanel panelHtml = (JPanel) panelB.getComponent(1);//得到htmldom选择panel
                    int childNumHtml = panelHtml.getComponentCount();
                    for(int r=0;r<childNumHtml-1;r++){
                        JPanel jPanelD = (JPanel) panelHtml.getComponent(r);//获取htmldom和索引的一块panel
                        MyText myText = (MyText) jPanelD.getComponent(0);//获取htmldom文本区域
                        MyText myTextIndex = (MyText) jPanelD.getComponent(1);//获取索引文本区域
                        if(!myText.getString().equals("")){
                            htmlDom.add(myText.getString());
                        }else {
                            //htmlDom.add(myText.getString());
                            break;
                        }
                        if(!myTextIndex.getString().equals("")){
                            indexList.add(Integer.valueOf(myTextIndex.getString()));
                        }else{
                            indexList.add(0);
                        }
                    }
                    ArrayList<String> inHtmlList = new ArrayList<>();//用来存放每一次解析到的所有的html匹配dom
                    if (!htmlDom.get(0).equals("")) {//判断第一块是否需要解析html，开始解析html
                        try {
                            Elements elements = null;
                            Element elementMain = document;
                            int sign = 0;
                            for (int a = 0; a < htmlDom.size(); a++) {
                                if(sign==1){
                                    sign++;//用sign来标志是否是第一次进入elements的获取
                                }
                                //当循环第一次a等于0，如果索引是-1那么搜索全文所有的匹配，如果大于-1那么搜索索引项的匹配
                                if(sign==0) {
                                    if (indexList.get(a) == -1) {
                                        elements = elementMain.select(htmlDom.get(a));
                                        sign ++;
                                    } else {
                                        elementMain = elementMain.select(htmlDom.get(a)).get(indexList.get(a));
                                    }
                                }else{
                                    if(indexList.get(a)==-1){
                                        JOptionPane.showMessageDialog(panel,"不能有两个索引为-1");
                                        return;
                                    }
                                }
                                if (a == htmlDom.size() - 1&&a!=0) {//如果循环到最后一个表达式，就获取表达式匹配的文本,确保不是只有一个
                                    if(indexList.get(a)==-1){//如果是最后一个那么不能循环获得，索引不能是-1
                                        if(rexText.equals("")){//如果不使用正则表达式，那么可以循环获得最后一个-1
                                            elements = elementMain.select(htmlDom.get(a));
                                            for(int f = 0;f<elements.size();f++){//循环读取最后一步的所有text添加进入列表
                                                if(!htmlDom.get(a).contains("=")&&htmlDom.get(a).contains("[")){
                                                    Pattern pattern = Pattern.compile("\\[(.*?)\\]");
                                                    Matcher matcher = pattern.matcher(htmlDom.get(a));
                                                    String attr = null;
                                                    if(matcher.find()) {
                                                        attr = matcher.group(1);
                                                    }else {
                                                        JOptionPane.showMessageDialog(panel,"最后一个元素的属性值正则匹配出错，请检查");
                                                    }
                                                    inHtmlList.add(elements.get(f).attr(attr));
                                                }else {
                                                    inHtmlList.add(elements.get(f).text());
                                                }
                                            }
                                        }else{//如果使用了正则表达式那么最后的索引不准是-1
                                            JOptionPane.showMessageDialog(panel, "同时使用正则表达式和htmldom那么最后一个索引不能是-1");
                                            return;
                                        }
                                    }else{//如果最后索引不是-1那么循环获得数据
                                        for (int y = 0; y < elements.size(); y++) {
                                            String temp;
                                            if(rexText.equals("")){//如果不使用正则表达式那么直接读取元素内部的text
                                                Elements elements1 = elements.get(y).select(htmlDom.get(a));
                                                if(elements1.size()==0){
                                                    continue;
                                                }else{

                                                    if(!htmlDom.get(a).contains("=")&&htmlDom.get(a).contains("[")){
                                                        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
                                                        Matcher matcher = pattern.matcher(htmlDom.get(a));
                                                        String attr = null;
                                                        if(matcher.find()) {
                                                            attr = matcher.group(1);
                                                        }else {
                                                            JOptionPane.showMessageDialog(panel,"最后一个元素的属性值正则匹配出错，请检查");
                                                        }
                                                        temp = elements1.get(indexList.get(a)).attr(attr);
                                                    }else {
                                                        temp = elements1.get(indexList.get(a)).text();
                                                    }
                                                }
                                            }else {
                                                temp = elements.get(y).select(htmlDom.get(a)).get(indexList.get(a)).outerHtml();
                                            }
                                            inHtmlList.add(temp);//把获取的东西添加进arrayList
                                        }
                                    }
                                    break;
                                }else if(a == htmlDom.size() - 1&&a==0){//如果只有一个htmldom那么取出的是outerhtml
                                    for(int f = 0;f<elements.size();f++){//循环读取最后一步的所有text添加进入列表
                                        inHtmlList.add(elements.get(f).outerHtml());
                                    }
                                    break;
                                }
                                if(sign>1) {
                                    for (int m = 0; m < elements.size(); m++) {
                                        //先把elements中的元素取出
                                        Element elementT = elements.get(m);
                                        //然后每一个元素反复匹配第二个到最后的dom匹配和索引
                                        Elements elements1 = elementT.select(htmlDom.get(a));
                                        if(elements1.size()>0){
                                            elementT = elements1.get(indexList.get(a));
                                            elements.remove(m);
                                            elements.add(m, elementT);
                                        }else {
                                            elements.remove(m);
                                        }
                                    }
                                }
                            }
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(panel, "出现异常");
                            return;
                        }
                        if(!rexText.equals("")){//在dom匹配之后就开始正则表达式匹配
                            int temp = inHtmlList.size();
                            for(int k = 0;k<temp;k++) {
                                Pattern pattern = Pattern.compile(rexText);
                                String html = inHtmlList.get(k);
                                Matcher matcher = pattern.matcher(html);
                                if(matcher.find()){
                                    html = matcher.group(rexIndexText);
                                    inHtmlList.remove(k);
                                    inHtmlList.add(k,html);
                                }
                            }
                        }
                    }//上面获取到的是第一块panel的html解析全文的结果，多个匹配
                    else{//这里是不需要html解析只使用正则表达式的
                        if(rexText.equals("")){
                            JOptionPane.showMessageDialog(panel,"两部分必须有一部分表达式");
                            return;
                        }else{
                            //这里是只使用正则表达式不使用htmldom解析的---------需要修改
                            Pattern pattern = Pattern.compile(rexText);
                            Matcher matcher = pattern.matcher(document.outerHtml());
                            String html;
                            while(matcher.find()){
                                html = matcher.group(rexIndexText);
                                inHtmlList.add(html);
                            }
                        }
                    }
                    //使用for循环把第一块读取的一轮数据写出去，每一部分占一行，都写完之后添加单独一行&&符号用来分隔
                    for(int m = 0;m<inHtmlList.size();m++){
                        write.write(inHtmlList.get(m)+"\r\n");
                    }
                    write.write("&&\r\n");
                }
                JOptionPane.showMessageDialog(panel,"读取成功");
            }catch (NullPointerException ex){
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel,"NullPointException");
            } catch (IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(panel,"写出失败");
            } finally {
                if(write!=null) {
                    try {
                        write.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

}
