import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.border.BevelBorder;
//jar cf Words.jar out/production/Nauka/Words.class
public class Words {
    public static final java.lang.String ILOSC_CWICZEN = "Ilosc cwiczen: ";
    private static final String DELIM = ";";
    private JFrame frame;
    private String logFile = "log.txt";
    private JComboBox wordsFiles = new JComboBox();
    private JTextField inputWord = new JTextField();
    private JLabel info = new JLabel();
    private JLabel outputWord = new JLabel();
    private JLabel timer = new JLabel();
    private JButton startLearn = new JButton("Start nauka");
    private JButton startExamNoLimit = new JButton("Start egzamin bez czasu");
    private JButton startExamLimit = new JButton("Start egzamin z czasem");
    private boolean reverse = false;
    private JButton reverseButton = new JButton("Odwróć");
    private JButton ok = new JButton("OK");
    private JButton next = new JButton(">");
    private JButton previous = new JButton("<");
    private JButton help = new JButton("Help");
    private JTextArea helpInfo = new JTextArea();
    private List<String> wordKey = new ArrayList<>();
    private List<String> wordValue = new ArrayList<>();
    private int index;
    private int totalWord;
    private int okWords;
    private int badWords;
    private boolean examMode = false;
    private static String dir = (new File("").getAbsolutePath()) + File.separator + "pliki" + File.separator;
    private static final int TIMEOUT = 60000;//5000 = 5sec
    private Timer showTime = new Timer(1000, new ClockListener());
    private int currentTime = TIMEOUT / 1000;
    private boolean useTime = false;

    public static void main(String args[]) {
        new Words();
    }

    public Words() {
        createView();
        readWordsFile();

    }

    private void readWordsFile() {
        try {
            File d = new File(dir);
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".txt");
                }
            };
            String[] files = d.list(filter);
            if (files == null || files.length == 0) {
                JOptionPane.showMessageDialog(null, "Nie ma zdefiniowanych plikow cwiczen. Aby korzystac z aplikacji nalezy przygotowac pliki *.txt w " + dir, "Warning", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            for (int i = 0; i < files.length; i++)
                wordsFiles.addItem(files[i]);
            wordsFiles.setSelectedIndex(0);

            wordsFilesMethod();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void move(boolean previous) {
        if (wordValue.isEmpty()|| examMode) return;
        if (previous) {
            index--;
            if (index < 0) {
                index = wordKey.size() - 1;
            }
        } else {
            index++;
            if (index == wordKey.size()) {
                index = 0;
            }
        }
        nextExercise();
    }

    private void okMethod() {
        if (wordValue.size() == 0) return;
        helpInfo.setText("");
        if (index < 0)
            startMethod();
        if (inputWord.getText().equals(""))
            return;
        boolean ok = false;
        StringTokenizer shouldBe = new StringTokenizer(wordValue.get(index), DELIM);
        String t;
        while (shouldBe.hasMoreTokens()) {
            t = shouldBe.nextToken().trim();
            if (t.equalsIgnoreCase(inputWord.getText().trim())) {
                ok = true;
            }
        }

        if (ok) {
            index++;
            okWords++;
            if (index < totalWord) {
                nextExercise();
            } else {
                examResult();
            }
        } else {
            if (examMode) {
                helpMethod();
                index++;
                badWords++;
                if (index < totalWord) {
                    nextExercise();
                } else {
                    examResult();
                }
            } else {
                inputWord.setText("");
            }

        }
    }

    private void nextExercise() {
        if (examMode) {
            info.setText(ILOSC_CWICZEN + totalWord + ". Sprawdzone: " + index + " (" + okWords + " dobrze, " + badWords + " zle). Pozostalo: " + (totalWord - index));
            currentTime = TIMEOUT / 1000;
            //timeout.stop();
            //timeout.start();
            //!!
        } else {
            info.setText(ILOSC_CWICZEN + totalWord + ". Nauczone: " + index + ". Pozostalo: " + (totalWord - index)+". Bieżące indeks: "+index);
        }
        outputWord.setText(wordKey.get(index));
        inputWord.setText("");
    }

    private void examResult() {
        info.setText("Koniec. Jeszcze raz?");

        String result = "";
        if ((((float) (okWords) / totalWord) * 100) >= 95)
            result = "Brawo! Rewelacja! <6>";
        else if ((((float) (okWords) / totalWord) * 100) >= 80)
            result = "Super! Swietnie! <5>";
        else if (((float) (okWords) / totalWord) * 100 >= 70)
            result = "O tak! Dobrze! <4>";
        else if ((((float) okWords) / totalWord) * 100 >= 50)
            result = "Moze byc... <3>";
        else if ((((float) okWords) / totalWord) * 100 >= 40)
            result = "Niedobrze... <2>";
        else
            result = "Bardzo zle. Cwicz dalej. <1>";

        result = result + "   [Ilosc: " + totalWord + ". OK: " + okWords + " Zle: " + badWords + "]";
//		result=result+"   [Ilość: "+totalWord+ ". OK: "+okWords+" Źle: "+(totalWord-okWords+"]");
        if (examMode) {
            JOptionPane.showMessageDialog(null, result, "Summary", JOptionPane.INFORMATION_MESSAGE);
            showTime.stop();
        }
        result = examMode ? "Exam mode " : "Learn mode " + result;

        try {
            File file = new File(logFile);
            RandomAccessFile r = new RandomAccessFile(file, "rw");
            r.seek(file.length());
            r.write((SimpleDateFormat.getDateInstance().format(new Date()) + " " + (String) wordsFiles.getSelectedItem()).getBytes());
            r.write((" " + result + " words count: " + totalWord).getBytes());
            r.write("\n".getBytes());
            r.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        index = -1;
    }

    private void startMethod() {
        //random();
        reverse();
        if (wordKey.size() == 0) return;
        outputWord.setText(wordKey.get(0));
        inputWord.setText("");
        helpInfo.setText("");
        index = 0;
        okWords = 0;
        badWords = 0;
        if (examMode) {
            info.setText(ILOSC_CWICZEN + totalWord + ". Sprawdzone: " + index + " (" + okWords + " dobrze, " + badWords + " źle). Pozostało: " + (totalWord - index));
            help.setVisible(false);
        } else {
            info.setText(ILOSC_CWICZEN + totalWord + ". Nauczone: " + index + ". Pozostalo: " + (totalWord - index));
            help.setVisible(true);
//			help.enable(true);
        }
        //helpUsed=false;
    }

    private void wordsFilesMethod() {
        try {
            System.out.println("Setting words from file " + wordsFiles.getSelectedItem());
            wordKey.clear();
            wordValue.clear();
            BufferedReader bfr = new BufferedReader(new FileReader(dir + wordsFiles.getSelectedItem()));
            String linia;
            while ((linia = bfr.readLine()) != null && linia.length() > 2 && linia.indexOf("=") > 0) {
                wordKey.add(linia.substring(0, linia.indexOf("=")).trim());
                wordValue.add(linia.substring(linia.indexOf("=") + 1, linia.length()).trim());
            }
            totalWord = wordKey.size();
            startMethod();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void random() {
        List<String> wordKeyTmp = new ArrayList();
        List<String> wordValueTmp = new ArrayList();
        Random r = new Random(System.currentTimeMillis());
        while (wordKey.size() > 0) {
            int number = r.nextInt(wordKey.size());
            System.out.println("number = " + number);
            if (reverse) {
                wordKeyTmp.add(wordValue.remove(number));
                wordValueTmp.add(wordKey.remove(number));
            } else {
                wordKeyTmp.add(wordKey.remove(number));
                wordValueTmp.add(wordValue.remove(number));
            }
        }
        wordKey = wordKeyTmp;
        wordValue = wordValueTmp;
    }

    private void reverse() {
        if (reverse) {
            System.out.println("Reverse words");
            List<String> wordKeyTmp = new ArrayList(wordKey);
            List<String> wordValueTmp = new ArrayList(wordValue);
            wordKey.clear();
            wordValue.clear();
            wordKey.addAll(wordValueTmp);
            wordValue.addAll(wordKeyTmp);
        }
    }

    private void helpMethod() {
        if (wordValue.size() == 0) return;
        //if noting entered, then there is printed an answer
        if (inputWord.getText().equals("")) {
            helpInfo.setText((String) wordValue.get(index));
            return;
        }
        //if there was correct answer then there is printed an answer
        StringTokenizer shouldBe = new StringTokenizer((String) wordValue.get(index), ",");
        while (shouldBe.hasMoreTokens()) {
            if (shouldBe.nextToken().equals(inputWord.getText())) {
                helpInfo.setText((String) wordValue.get(index));
                return;
            }
        }
        //if there was incorrect answer then there is printed input text and an answer
        helpInfo.setText("Dla " + wordKey.get(index) + "\n" + "wprowadzono:'" + inputWord.getText() + "'  Poprawnie:'" + wordValue.get(index)
                + "'");

    }

    private void actionForKey(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            okMethod();
        }
    }

    private void createView() {
        JFrame.setDefaultLookAndFeelDecorated(true);

        frame = new JFrame("Words Application");

        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okMethod();
            }
        });
        previous.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                move(true);
            }
        });
        next.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                move(false);
            }
        });
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                helpMethod();
            }
        });
        startLearn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //startLearnMethod();
                examMode = false;
                showTime.stop();
                startMethod();
            }
        });
        startExamNoLimit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                useTime = false;
                currentTime = TIMEOUT / 1000;
                showTime.start();

                examMode = true;
                startMethod();
            }
        });
        startExamLimit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                useTime = true;
                currentTime = TIMEOUT / 1000;
                showTime.start();

                examMode = true;
                startMethod();
            }
        });
        reverseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (reverse) {
                    reverse = false;
                } else {
                    reverse = true;
                }
                System.out.println("Current reverse: " + reverse);
                startMethod();
            }
        });
        wordsFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                wordsFilesMethod();
            }
        });

        outputWord.setBorder(new BevelBorder(1));

        inputWord.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                actionForKey(e);
            }

            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }
        });

        JPanel mainPanel = new JPanel(new GridLayout(9, 1));

        mainPanel.add((new JLabel(" * * *  Witaj! Bawimy się i uczymy  * * *")));
        mainPanel.add(wordsFiles);

        JPanel startsPanel = new JPanel(new GridLayout(1, 4));
        startsPanel.add(startLearn);
        startsPanel.add(startExamLimit);
        startsPanel.add(startExamNoLimit);
        startsPanel.add(reverseButton);

        mainPanel.add(startsPanel);

        mainPanel.add(info);
        mainPanel.add(outputWord);
        mainPanel.add(inputWord);

        JPanel okPanel = new JPanel(new GridLayout(1, 3));
        okPanel.add(previous);
        okPanel.add(next);
        okPanel.add(ok);

        mainPanel.add(okPanel);

        JPanel helpPanel = new JPanel(new GridLayout(1, 2));
        helpPanel.add(help);
        helpPanel.add(helpInfo);
        mainPanel.add(helpPanel);
        mainPanel.add(helpPanel);
        timer.setFont(new Font("sansserif", Font.PLAIN, 38));
        mainPanel.add(timer);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setSize(700, 700);
        frame.setVisible(true);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        frame.setLocation(screenWidth / 4, screenHeight / 4);
    }

    class ClockListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!useTime) {
                Calendar now = Calendar.getInstance();
                int h = now.get(Calendar.HOUR_OF_DAY);
                int m = now.get(Calendar.MINUTE);
                int s = now.get(Calendar.SECOND);
                timer.setText("" + h + ":" + m + ":" + s);
            } else {
                timer.setText("" + (currentTime--));
                if ((currentTime) == 0) {
                    currentTime = TIMEOUT / 1000;
                    inputWord.setText("timout");
                    if (examMode) {
                        okMethod();
                    }
                }
            }
        }
    }

    class TimeoutListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
//            inputWord.setText("timout");
            //          okMethod();
        }
    }
}