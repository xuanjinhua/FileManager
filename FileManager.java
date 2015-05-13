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
import javax.swing.table.TableModel;
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
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class FileManager extends JFrame implements Runnable
{
  JPanel mainPanel;
  JTabbedPane viewerPane;
  JPanel contentPanel;
  JPanel filesPanel;
  JPanel sorterPanel;
  JTable files;
  JTextArea sorters;
  DefaultTableModel model; 
  DefaultTableModel fileModel;
  JTable table;
  String currentFolder;
  String col[] = {"File Name", "Date Added"};
  String col1[] = {"Folders"};
  ArrayList<String> uploadedFiles;
  Map<Integer, JTextArea> indexToTextArea;
  File newF;
  File currentFile;
  File savedFile;
  File homeBase;
  File[] directories; 
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
    newF = new File(homeBase + "\\All Documents");
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
    for(String str: uploadedFiles)
    {
      sorters.append(str + "\n");
    } 
    makeMenus();
    makeTable();
    printDirectories();
    table.addMouseListener(new MouseAdapter() {
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
          try{
            displayFile();
          }
          catch(IOException ex){
            ex.printStackTrace();
          }
        }
      }
    });
    viewerPane.addMouseListener(new MouseAdapter() {
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
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
          int row = files.getSelectedRow();
          currentFolder = homeBase.getAbsolutePath() + "\\" + files.getValueAt(row,0);
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
    JMenuItem addFoldersItem = new JMenuItem("Add Folders...");
    JMenuItem quitItem = new JMenuItem("Quit");
    JMenuItem rmFilesItem = new JMenuItem("Remove File(s)");
    JMenuItem rmFoldersItem = new JMenuItem("Remove Folder(s)");
    fileMenu.add(addFilesItem);
    fileMenu.add(addFoldersItem);
    fileMenu.add(quitItem);
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
        try
        {
          currentFile = jfc.getSelectedFile();
          savedFile = new File(homeBase.getAbsolutePath() + "\\All Documents\\" + currentFile.getName());
          saveFile();
          savedFile = new File(currentFolder + "\\" + currentFile.getName());
          saveFile();
          sorters.setText("");
          for(String str: uploadedFiles)
          {
            sorters.append(str + "\n");
          }
          model.addRow(new Object[]{null, null});
          makeTable();
        }
        catch(FileNotFoundException ex)
        {
          System.out.println("File " + currentFile.getAbsolutePath() + " not found. ");
        }
        catch(IOException ex)
        {
          ex.printStackTrace();
        }
      }
    });
    addFoldersItem.addActionListener( e -> {
      JTextField textField = new JTextField();
      JPanel p = new JPanel(new BorderLayout(2,2));
      p.add(textField);
      JOptionPane jop = new JOptionPane(JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
      int returnVal = JOptionPane.showConfirmDialog(this, p, "ADD", JOptionPane.YES_NO_OPTION);
      if (returnVal == JOptionPane.OK_OPTION)
      {
        new File(homeBase.getAbsolutePath()+ "\\" + textField.getText()).mkdir();
        directories = new File(homeBase.getAbsolutePath()).listFiles(File::isDirectory);
        fileModel.addRow(new Object[]{null, null});
        printDirectories();
      }
    });
    rmFilesItem.addActionListener( e -> {
      int[] rows = table.getSelectedRows();
      List<Integer> modelRows = new ArrayList<Integer>(rows.length);     
      for (int row: rows)
      {
        int modelRow = table.convertRowIndexToModel( row );
        modelRows.add( new Integer(modelRow) );
      }
      Collections.sort(modelRows, Collections.reverseOrder());
      DefaultTableModel model = (DefaultTableModel)table.getModel();
      for (Integer row: modelRows)
      {
          File f = new File(currentFolder + "\\" + table.getValueAt(row, 0));
          File newf = new File(homeBase.getAbsolutePath() + "\\Trash\\" + table.getValueAt(row, 0));
          f.renameTo(newf);
          uploadedFiles.remove(table.getValueAt(row, 0));
          model.removeRow(row);
      }
      sorters.setText("");
      for(String str: uploadedFiles)
      {
        sorters.append(str + "\n");
      }
    });
    rmFoldersItem.addActionListener( e -> {
      int[] rows = files.getSelectedRows();
      List<Integer> modelRows = new ArrayList<Integer>(rows.length);     
      for (int row: rows)
      {
        int modelRow = files.convertRowIndexToModel( row );
        modelRows.add( new Integer(modelRow) );
      }
      Collections.sort(modelRows, Collections.reverseOrder());
      DefaultTableModel model = (DefaultTableModel)files.getModel();
      for (Integer row: modelRows)
      {
        File f = new File(homeBase.getAbsolutePath() + "//" + files.getValueAt(row, 0));
        if(f.list().length > 0)
        {
          for(File del: f.listFiles())
          {
            del.delete();
          }
        }
        f.delete();
        model.removeRow(row);
      }
    });
  }
  private void saveFile() throws IOException
  {
    try{
      InputStream in = new FileInputStream(currentFile);
      OutputStream out = new FileOutputStream(savedFile);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0){
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
      uploadedFiles = new ArrayList<>(Arrays.asList(newF.list()));
    }
    catch(FileNotFoundException ex){
      System.out.println(ex.getMessage() + " in the specified directory.");
      System.exit(0);
    }
    catch(IOException e){
      System.out.println(e.getMessage());      
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
          Path p = Paths.get((String)file.getAbsolutePath());
          BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
          if(k < table.getRowCount())
          {
            table.setValueAt(file.getName(),k,0);
            table.setValueAt(attr.creationTime(),k,1);
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
  private void displayFile() throws IOException
  {
    File file = new File(currentFolder + "\\" + viewerPane.getTitleAt(viewerPane.getSelectedIndex()));
    FileReader fr = new FileReader(file);
    int ch;
    StringBuffer sb = new StringBuffer();
    while( (ch = fr.read()) !=EOF)
    {
      sb.append((char) ch);
    }
    JTextArea viewer =  indexToTextArea.get(viewerPane.getSelectedIndex());
    viewer.setText(sb.toString());
  }
  public static void main(String[] args)
  {
    FileManager fm = new FileManager();
    javax.swing.SwingUtilities.invokeLater(fm);
  }
} 
