
  package genepi.mut.annotate;
  
  //import static org.junit.Assert.assertEquals;
  
  import java.io.IOException;
  
  import genepi.base.Tool; import lukfor.tables.Table; import
  lukfor.tables.columns.IBuildValueFunction; import
  lukfor.tables.columns.types.StringColumn; import
  lukfor.tables.io.TableBuilder; import lukfor.tables.io.TableWriter; import
  lukfor.tables.rows.Row;
  
  public class BaseAnnotate extends Tool {
  
  public BaseAnnotate(String[] args) { super(args); 
  // TODO Auto-generated constructor stub 
  }
 
  
  @Override public void createParameters() { addParameter("input",
  "input variant file"); addParameter("annotation",
  "input annotation file, e.g. in resources: "); addParameter("output",
  "output variant file"); }
  
  @Override public void init() { 
	  // TODO Auto-generated method stub 
	  }
  
  @Override public int run() {
  
  String input = (String) getValue("input");
  
  String annotation = (String) getValue("annotation");
  
  String output = (String) getValue("output");
  
  try { Table table;
  
  table = TableBuilder.fromFile(input, '\t'); table.getColumns().append(new
  StringColumn("Mutation"), new IBuildValueFunction() { public String
  buildValue(Row row) throws IOException { return row.getInteger("Pos") +
  row.getString("Variant"); } });
  
  //assertEquals(12, table.getColumns().getSize()); 
  Table table2 =   TableBuilder.fromFile(annotation, '\t'); 
  //assertEquals(24,  table2.getColumns().getSize()); 
  table.merge(table2, "Mutation");
  //assertEquals(35, table.getColumns().getSize()); 
  //assertEquals(83,  table.getRows().getSize());
  
  TableWriter.writeToCsv(table, output, '\t'); } catch (IOException e) { //
  //TODO Auto-generated catch block 
	  e.printStackTrace(); }
  
  return 0; }
  
  public static void main(String[] args) {
  
  BaseAnnotate annoTool = new BaseAnnotate(args);
  
  annoTool = new BaseAnnotate(new String[] { "--input",
  "test-data/mtdna/mixtures/M4-M5.txt", "--annotation",
  "test-data/mtdna/annotation/annotation_2020-08-14.txt", "--output",
  "test-data/mtdna/annotation/M4-M5-annotated.txt"});
  
  annoTool.start();
  
  }
  
  }
 