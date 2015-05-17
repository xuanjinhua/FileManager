//*********************************************
// FileEditor V 1.0
// Author: RPBruiser
// Date: 05-16-2015
//*********************************************

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.BorderLayout;

public class FileEditor extends JFrame implements Runnable, DocumentListener
{
  JMenuBar mbar;
  JMenu fileMenu;
  JMenuItem save;
  JEditorPane textView;
  static JFrame frame;
  File file;
  File folder;
  File home = new File(new File(".").getAbsolutePath());
  boolean isSaved;
  public static final int EOF;
  static
  {
    EOF = -1;
  }
  
  public FileEditor(File _folder)
  {
    super("FileEditor:  Untitled");
    save = new JMenuItem("Save As...");
    isSaved = false;
    file = null;
    folder = _folder;
    textView = new JEditorPane();
    textView.getDocument().addDocumentListener(this);
  }
  public FileEditor(File _file, File _folder)
  {
    super("FileViewer:  " + _file.getName());
    save = new JMenuItem("Save");
    isSaved = true;
    file = new File(home + File.separator + _folder.getName() + File.separator + _file.getName());
    folder = _folder;
    textView = new JEditorPane();
    textView.getDocument().addDocumentListener(this);
  }
  @Override
  public void run()
  {
    frame = this;
    setSize(1200, 700);
    makeFileMenu();
    displayFile();
    getContentPane().add(new JScrollPane(textView));
    setVisible(true);
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  }
  private void makeFileMenu()
  {
    mbar = new JMenuBar();
    setJMenuBar(mbar);
    fileMenu = new JMenu("File");
    mbar.add(fileMenu);
    fileMenu.add(save);
    save.addActionListener((ActionEvent e) -> {
        try {
            if(isSaved)
            {
                return;
            }
            boolean fileIsChosen = false;
            if(file != null)
            {
                fileIsChosen = true;
            }
            if(!fileIsChosen)
            {
                JTextField textField = new JTextField();
                JPanel p = new JPanel(new BorderLayout(2,2));
                p.add(textField);
                JOptionPane jop = new JOptionPane(JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
                int returnVal = JOptionPane.showConfirmDialog(textView, p, "ADD", JOptionPane.YES_NO_OPTION);
                if (returnVal == JOptionPane.OK_OPTION)
                {
                    file = new File(folder + File.separator + textField.getText());
                    try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
                        out.write(textView.getText());
                    }
                    if(!folder.equals(new File(home.getAbsolutePath() + File.separator + "All-Documents")))
                    {
                        Path newLink = Paths.get((String)home.getAbsolutePath() + File.separator + "All-Documents" + File.separator + textField.getText());
                        Path existingFile = Paths.get((String)file.getAbsolutePath());
                        Files.createLink(newLink, existingFile);
                    }
                    isSaved = true;
                    fixTitleBar();
                }
            }
            else{
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                out.write(textView.getText());
                out.close();
                isSaved = true;
                fixTitleBar();
            }
        } catch (IOException x1) {
            x1.printStackTrace();
        }
    });
  }
  public void displayFile()
  {
    if(file != null)
    {
      try{
        FileReader fr = new FileReader(file);
        int ch;
        StringBuilder sb = new StringBuilder();
        while( (ch = fr.read()) !=EOF)
        {
          sb.append((char) ch);
        }
        textView.setText(sb.toString());
      }
      catch(IOException e){
        e.printStackTrace();
      }
    }
  }
  public void fixTitleBar()
  {
    String titleString = "FileViewer:  ";
    if(file == null)
      titleString += "Untitled";
    else
    {
      titleString += file.getName();
    }
    if(!isSaved)
      titleString += " *";
    setTitle(titleString);
  }
  @Override
  public void changedUpdate(DocumentEvent e)
  {
    isSaved = false;
    fixTitleBar();
  }
  @Override
  public void insertUpdate(DocumentEvent e)
  {
    isSaved = false;
    fixTitleBar();
  }
  @Override
  public void removeUpdate(DocumentEvent e)
  {
    isSaved = false;
    fixTitleBar();
  }
  public static void main(String[] args)
  {
    if(args.length < 2)
    {
      FileEditor fe = new FileEditor(new File(args[0]));
      javax.swing.SwingUtilities.invokeLater(fe);
    }
    else
    {
      FileEditor fe = new FileEditor(new File(args[0]), new File(args[0] + File.separator + args[1]));
      javax.swing.SwingUtilities.invokeLater(fe);
    }
  }
}
