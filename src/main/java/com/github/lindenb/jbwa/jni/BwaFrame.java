package com.github.lindenb.jbwa.jni;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;


public class BwaFrame extends JFrame
	{
	private final static String COLS[]=new String[]{"CHROM","POS","STRAND","CIGAR","MQUAL","NM","Secondary"};
	private class AlnTableModel  extends AbstractTableModel
		{
		private Vector<AlnRgn> array=new Vector<AlnRgn>();
		@Override
		public String getColumnName(int column) {
			return  COLS[column];
			}
		@Override
		public int getColumnCount() {
			return COLS.length;
			}
		@Override
		public int getRowCount() {
			return array.size();
			}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
			{
			AlnRgn a=this.array.get(rowIndex);
			switch(columnIndex)
				{
				case 0: return a.getChrom();
				case 1: return a.getPos();	
				case 2: return a.getStrand();	
				case 3: return a.getCigar();
				case 4: return a.getMQual();	
				case 5: return a.getNm();
				case 6: return a.getSecondary();	
				default: return null;
				}
			}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex)
				{
				case 0: return String.class;
				case 1: return Long.class;
				case 2: return Character.class;
				case 3: return String.class;
				case 4: return Integer.class;
				case 5: return Integer.class;
				case 6: return Integer.class;
				default: return Object.class;
				}
			}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
			}
		void clear()
			{
			array.clear();
			fireTableDataChanged();
			}
		void addAll(AlnRgn rgn[])
			{
			array.clear();
			if(rgn!=null) for(AlnRgn a:rgn) array.add(a);
			fireTableDataChanged();
			}
		}
 	private AlnTableModel tableModel;
 	private JTextField seqField;
 	private BwaIndex bwaIndex;
	private BwaFrame(File f,BwaIndex bwaIndex)
		{
		super("JBWA:"+f);
		this.bwaIndex=bwaIndex;
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowClosing(WindowEvent e) {
				doMenuClose();
				}
			});
		JMenuBar bar=new JMenuBar();
		setJMenuBar(bar);
		JPanel mainPane=new JPanel(new BorderLayout(5,5));
		mainPane.setBorder(new EmptyBorder(5,5,5,5));
		setContentPane(mainPane);
		
		JPanel pane=new JPanel(new FlowLayout(FlowLayout.LEADING));
		mainPane.add(pane,BorderLayout.NORTH);
		this.seqField=new JTextField(50);
		pane.add(seqField);
		Action action=new AbstractAction("Align")
			{
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doMenuAlign();
				}
			};
		seqField.addActionListener(action);
		seqField.setText("CCAANCGCGAGAAGATGACCCAGATCATGTTTGAGACCTTCAACACCCCAGCCATGTACGTGGAGATCGGAAGAGCACACGTCTGAACTCCAGTCACCAA");
		pane.add(new JButton(action));
		
		this.tableModel=new AlnTableModel();
		JTable table=new JTable(tableModel);
		table.setFont(new Font("Courier",0, 20));
		table.setRowHeight(25);
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		mainPane.add(new JScrollPane(table),BorderLayout.CENTER);
		
		JMenu menu=new JMenu("File");
		menu.add(action);
		menu.add(new AbstractAction("Quit")
			{
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doMenuClose();
				}
			});
		
		}
	private void doMenuClose()
		{
		this.bwaIndex.close();
		this.setVisible(false);
		this.dispose();
		}
	private void doMenuAlign()
		{
		this.tableModel.clear();
		String dna=this.seqField.getText().trim().toUpperCase();
		if(dna.length()<10 || !dna.matches("[ATGNC]+"))
			{
			JOptionPane.showMessageDialog(this, "Bad DNA","Error",JOptionPane.ERROR_MESSAGE);
			return;
			}
		ShortRead read=new ShortRead("Any",dna.getBytes(),dna.replaceAll("[ANTGC]","I").getBytes());
		BwaMem mem=null;
		try
			{
			mem=new BwaMem(this.bwaIndex);
			this.tableModel.addAll(mem.align(read));
			mem.dispose();
			}
		catch(Exception err)
			{
			err.printStackTrace();
			JOptionPane.showMessageDialog(this,"BWA-ERROR","Error",JOptionPane.ERROR_MESSAGE);
			}
		finally
			{
			if(mem!=null) mem.dispose();
			}	
		}
	public static void main(String[] args)
		{
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		System.loadLibrary("bwajni");
		File startFile=null;
		if(args.length>0)
			{
			startFile=new File(args[0]);
			if(startFile.isFile()) startFile=startFile.getParentFile();
			}
		
		JFileChooser selFile=new JFileChooser(startFile);
		selFile.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription()
				{
				return "BWA indexed file";
				}
			
			@Override
			public boolean accept(File f) {
				if(!f.isFile()) return true;
				String name=f.getName().toLowerCase();
				return name.endsWith(".fa.gz") || name.endsWith(".fa") ||
					name.endsWith(".fasta.gz") || name.endsWith(".fasta");
				}
			});
		if(selFile.showOpenDialog(null)!=JFileChooser.APPROVE_OPTION) return;
		File fileIndex=selFile.getSelectedFile();
		if(fileIndex==null) return;
		System.out.println("Loading "+fileIndex+"...");
		BwaIndex index=null;
		try
			{
			index=new BwaIndex(fileIndex);
			}
		catch (Exception e) {
			System.err.println("Cannot read "+fileIndex);
			e.printStackTrace();
			return;
			}

		final BwaFrame frame=new BwaFrame(fileIndex,index);
		Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(50, 50, screen.width-100, screen.height-100);

		try {
			SwingUtilities.invokeAndWait(new Runnable()
				{
				@Override
				public void run()
					{
					frame.setVisible(true);	
					}
				});
			
			} 
		catch (Exception e) {
			e.printStackTrace();
			}
		}
	}
