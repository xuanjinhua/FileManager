//*********************************************
// FileManager V 1.0
// Author: RPBruiser
// Date: 05-16-2015
//*********************************************

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class FileManager extends JFrame implements Runnable
{
  private final JPanel mainPanel;
  private final JTabbedPane viewerPane;
  private final JPanel contentPanel;
  private final JPanel filesPanel;
  private final JPanel sorterPanel;
  private JTable files;
  private final JTextArea sorters;
  private DefaultTableModel model; 
  private DefaultTableModel fileModel;
  private JTable table;
  public String currentFolder;
  private final String col[] = {"File Name", "Date Added"};
  private final String col1[] = {"Folders"};
  private ArrayList<String> uploadedFiles;
  private final Map<Integer, JTextArea> indexToTextArea;
  public File newF;
  public File currentFile;
  public File savedFile;
  public File homeBase;
  public File[] directories; 
  public static final int EOF;
  static
  {
    EOF = -1;
  }
  public FileManager()
  {
    super("My File Manager");
    currentFile = null;
    savedFile = null;
    homeBase = new File(new File(".").getAbsolutePath());
    directories = new File(homeBase.getAbsolutePath()).listFiles(File::isDirectory);
    indexToTextArea = new HashMap<>();
    newF = new File(homeBase + File.separator + "All-Documents");
    mainPanel = new JPanel();
    contentPanel = new JPanel();
    filesPanel = new JPanel();
    sorterPanel = new JPanel();
    viewerPane = new JTabbedPane();
    uploadedFiles = new ArrayList<>(Arrays.asList(newF.list()));
    sorters = new JTextArea();
    sorters.setEditable(false);
    currentFolder = newF.getAbsolutePath();
  }
  @Override 
  public void run()
  {
    setSize(1200,700);
    getContentPane().add(mainPanel);
    model = new DefaultTableModel(col,(new File(currentFolder).list().length))
    {
      @Override 
      public boolean isCellEditable(int arg0, int arg1) 
      { 
        return false; 
      }
    };
    fileModel = new DefaultTableModel(col1,directories.length)
    {
      @Override 
      public boolean isCellEditable(int arg0, int arg1) 
      { 
        return false; 
      }
    }; 
    table = new JTable(model);
    table.getTableHeader().setReorderingAllowed(false);
    files = new JTable(fileModel);
    files.getTableHeader().setReorderingAllowed(false);
    mainPanel.setLayout(new GridLayout(1,2));
    mainPanel.add(contentPanel);
    mainPanel.add(viewerPane);
    contentPanel.setLayout(new GridLayout(2,1));
    contentPanel.add(new JScrollPane(files));
    contentPanel.add(new JScrollPane(sorters));
    sorters.setText("");
    uploadedFiles.stream().forEach((str) -> {
      sorters.append(str + System.lineSeparator());
    }); 
    makeMenus();
    makeTable();
    printDirectories();
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          int row = table.getSelectedRow();
          JTextArea textArea = new JTextArea();
          textArea.setEditable(false);
          JScrollPane jsp = new JScrollPane();
          jsp.setViewportView(textArea);
          viewerPane.addTab((String) table.getValueAt(row, 0), null , new JPanel().add(jsp), (String) table.getValueAt(row, 0));
          viewerPane.setSelectedIndex(viewerPane.getComponentCount()-1);
          indexToTextArea.put(viewerPane.getComponentCount()-1, textArea);
          displayFile();
        }
      }
    });
    viewerPane.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          int i = viewerPane.getSelectedIndex();
          if (i >= 1) {
            viewerPane.remove(i);
          }
        }
      }
    });
    files.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
          int row = files.getSelectedRow();
          currentFolder = homeBase.getAbsolutePath() + File.separator + files.getValueAt(row,0);
          makeTable();
          model.fireTableDataChanged();
        }
      }
    });
    viewerPane.add("Files", table);
    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }
  private void makeMenus()
  {
    JMenuBar mbar = new JMenuBar();
    setJMenuBar(mbar);
    JMenu fileMenu = new JMenu("File");
    JMenu editMenu = new JMenu("Edit");
    mbar.add(fileMenu);
    mbar.add(editMenu);
    JMenuItem addFilesItem = new JMenuItem("Import Files...");
    JMenuItem makeFilesItem = new JMenuItem("Make Files...");
    JMenuItem addFoldersItem = new JMenuItem("Add Folders...");
    JMenuItem quitItem = new JMenuItem("Quit");
    JMenuItem editFilesItem = new JMenuItem("Edit File(s)...");
    JMenuItem rmFilesItem = new JMenuItem("Remove File(s)");
    JMenuItem rmFoldersItem = new JMenuItem("Remove Folder(s)");
    fileMenu.add(addFilesItem);
    fileMenu.add(makeFilesItem);
    fileMenu.add(addFoldersItem);
    fileMenu.add(quitItem);
    editMenu.add(editFilesItem);
    editMenu.add(rmFilesItem);
    editMenu.add(rmFoldersItem);
    quitItem.addActionListener( e -> {
      System.exit(0);
    });
    addFilesItem.addActionListener( e -> {
      JFileChooser jfc = new JFileChooser();
      int choice = jfc.showOpenDialog(FileManager.this);
      if(choice == JFileChooser.APPROVE_OPTION)
      {
        currentFile = jfc.getSelectedFile();
        savedFile = new File(homeBase.getAbsolutePath() + File.separator + "All-Documents"+ File.separator + currentFile.getName());
        saveFile();
        savedFile = new File(currentFolder + File.separator + currentFile.getName());
        saveFile();
        sorters.setText("");
        uploadedFiles.stream().forEach((str) -> {
          sorters.append(str + System.lineSeparator());
        });
        model.addRow(new Object[]{null, null});
        makeTable();
      }
    });
    makeFilesItem.addActionListener( e -> {
      String[] args = new String[1];
      args[0] = currentFolder;
      FileEditor fe = new FileEditor(new File(args[0]));
      fe.run();
      fe.addWindowListener( new WindowAdapter(){
        public void windowClosing(WindowEvent e)
        {
          fe.setVisible(false);
           try{
            uploadedFiles.add(fe.file.getName());
            sorters.setText("");
            uploadedFiles.stream().forEach((str) -> {
              sorters.append(str + System.lineSeparator());
            });
            makeTable();
          }
          catch(Exception ex) {
          }
        }
      });
    });
    addFoldersItem.addActionListener( e -> {
      JTextField textField = new JTextField();
      JPanel p = new JPanel(new BorderLayout(2,2));
      p.add(textField);
      JOptionPane jop = new JOptionPane(JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
      int returnVal = JOptionPane.showConfirmDialog(this, p, "ADD", JOptionPane.YES_NO_OPTION);
      if (returnVal == JOptionPane.OK_OPTION)
      {
        new File(homeBase.getAbsolutePath()+ File.separator + textField.getText()).mkdir();
        directories = new File(homeBase.getAbsolutePath()).listFiles(File::isDirectory);
        fileModel.addRow(new Object[]{null, null});
        printDirectories();
      }
    });
    editFilesItem.addActionListener( e -> {
      String[] args = new String[2];
      args[1] = currentFolder;
      int[] rows = table.getSelectedRows();
      List<Integer> modelRows = new ArrayList<>(rows.length);     
      for (int row: rows)
      {
        int modelRow = table.convertRowIndexToModel( row );
        modelRows.add(modelRow);
      }
      Collections.sort(modelRows, Collections.reverseOrder());
      DefaultTableModel model = (DefaultTableModel)table.getModel();
      for (Integer row: modelRows)
      {
        File f = new File(currentFolder + File.separator + table.getValueAt(row, 0));
        args[0] = f.getAbsolutePath();
        FileEditor fe = new FileEditor(new File(args[0]), new File(args[1]));
        fe.run();
        fe.addWindowListener( new WindowAdapter(){
          public void windowClosing(WindowEvent e)
          {
            fe.setVisible(false);
            makeTable();
          }
        });
      }
    });
    rmFilesItem.addActionListener( e -> {
      int[] rows = table.getSelectedRows();
      List<Integer> modelRows = new ArrayList<>(rows.length);     
      for (int row: rows)
      {
        int modelRow = table.convertRowIndexToModel( row );
        modelRows.add(modelRow);
      }
      Collections.sort(modelRows, Collections.reverseOrder());
      DefaultTableModel model = (DefaultTableModel)table.getModel();
      for (Integer row: modelRows)
      {
        File f = new File(currentFolder + File.separator + table.getValueAt(row, 0));
        File newf = new File(homeBase.getAbsolutePath() + File.separator + "Trash" + File.separator + table.getValueAt(row, 0));
        f.renameTo(newf);
        if(f.renameTo(newf) == false || new File(currentFolder).getName().equals("Trash"))
        {
          f.delete();
        }
        File del = new File(newF + File.separator + table.getValueAt(row, 0));
        del.delete();
        uploadedFiles.remove(table.getValueAt(row, 0));
        model.removeRow(row);
      }
      sorters.setText("");
      uploadedFiles.stream().forEach((str) -> {
        sorters.append(str + System.lineSeparator());
      });
    });
    rmFoldersItem.addActionListener( e -> {
      int[] rows = files.getSelectedRows();
      List<Integer> modelRows = new ArrayList<>(rows.length);     
      for (int row: rows)
      {
        int modelRow = files.convertRowIndexToModel( row );
        modelRows.add(modelRow);
      }
      Collections.sort(modelRows, Collections.reverseOrder());
      DefaultTableModel model = (DefaultTableModel)files.getModel();
      modelRows.stream().forEach((row) -> {
        File f = new File(homeBase.getAbsolutePath() + File.separator + files.getValueAt(row, 0));
        if(f.list().length > 0)
        {
          for(File del: f.listFiles())
          {
            del.delete();
          }
        }
        f.delete();
        model.removeRow(row);
      });
    });
  }
  private void saveFile()
  {
    try{
      Path newLink = Paths.get(savedFile.getAbsolutePath());
      Path existingFile = Paths.get(currentFile.getAbsolutePath());
      Files.createLink(newLink, existingFile);
      uploadedFiles = new ArrayList<>(Arrays.asList(newF.list()));
    }
    catch(FileNotFoundException ex){
      System.out.println(ex.getMessage() + " in the specified directory.");
    }
    catch(IOException e){
      System.out.println(e.getMessage());      
    }
    catch (UnsupportedOperationException x){
      System.err.println(x.getMessage());
    }
  }
  public void makeTable()
  {
    try{
      int k = 0;
      File folder = new File(currentFolder);
      if(folder.list().length > 0)
      {
        for(File file: folder.listFiles())
        {
          Path p = Paths.get(file.getAbsolutePath());
          BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
          if(k < table.getRowCount())
          {
            table.setValueAt(file.getName(),k,0);
            table.setValueAt(attr.lastModifiedTime(),k,1);
            k++;
            while(table.getRowCount() > folder.list().length)
            {
              model.removeRow(k);
            }
          }
          else if(k >= table.getRowCount())
          {
            Object[] row = new Object[2];
            row[0] = file.getName();
            row[1] = attr.creationTime();
            model.addRow(row);
            k++;
          }
        }
      }
      else
      {
        while(table.getRowCount() > folder.list().length)
        {
          model.removeRow(0);
        }
      }
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }
  public void printDirectories()
  {
    for(int k = 0; k < directories.length; k++)
    {
      files.setValueAt(directories[k].getName(),k,0);
    }
  }
  private void displayFile()
  {
    try{
      File file = new File(currentFolder + File.separator + viewerPane.getTitleAt(viewerPane.getSelectedIndex()));
      FileReader fr = new FileReader(file);
      int ch;
      StringBuilder sb = new StringBuilder();
      while( (ch = fr.read()) !=EOF)
      {
        sb.append((char) ch);
      }
      JTextArea viewer =  indexToTextArea.get(viewerPane.getSelectedIndex());
      viewer.setText(sb.toString());
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }
  public static void main(String[] args)
  {
    FileManager fm = new FileManager();
    javax.swing.SwingUtilities.invokeLater(fm);
  }
}
