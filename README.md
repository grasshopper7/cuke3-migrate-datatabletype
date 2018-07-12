Work in Progress -- XStream in Java Cucumber implementation is dead. No more trying to decide whether to extend AbstractSingleValueConverter or implement Converter.

For the official announcement mentioning other goodies, wander to this link - https://cucumber.io/blog/2018/05/19/announcing-cucumber-jvm-3-0-0.

For the release notes navigate to - https://github.com/cucumber/cucumber-jvm/blob/master/CHANGELOG.md and scroll down to the 3.0.0-SNAPSHOT section. In this check out point 2 where annotations like @Delimiter, @Format, @Transformer,@XStreamConverter, @XStreamConverters are laid to rest. These must be replaced by a DataTableType or ParameterType.

For details on Cucumber Expressions which have been introduced to work alongside Regular Expressions refer to this - https://docs.cucumber.io/cucumber/cucumber-expressions/

Source Code – Have tried to add the relevant code portions in the article. For bigger source code will point to the relevant link.

Refer to [cuke2-parameter-datatable](https://github.com/grasshopper7/cuke2-parameter-datatable) link for Cucumber 2. Scenarios are contained in [parameter.feature](https://github.com/grasshopper7/cuke2-parameter-datatable/blob/master/cuke2-parameter-datatable/src/test/resources/features/parameter.feature). Step Definition in [ParameterStepDefinition.java](https://github.com/grasshopper7/cuke2-parameter-datatable/blob/master/cuke2-parameter-datatable/src/test/java/stepdef/ParameterStepDefinition.java). 

Refer to [cuke3-migrate-datatabletype](https://github.com/grasshopper7/cuke3-migrate-datatabletype) for Cucumber 3. Scenarios are contained in [datatabletype.feature](https://github.com/grasshopper7/cuke3-migrate-datatabletype/blob/master/cuke3-migrate-datatabletype/src/test/resources/features/datatabletype.feature). Step Definition code in [DataTableTypeStepDefinition.java](https://github.com/grasshopper7/cuke3-migrate-datatabletype/blob/master/cuke3-migrate-datatabletype/src/test/java/stepdef/DataTableTypeStepDefinition.java). Parameter registration code in [Configurer.java](https://github.com/grasshopper7/cuke3-migrate-datatabletype/blob/master/cuke3-migrate-datatabletype/src/test/java/stepdef/Configurer.java).

What is DataTableType? - This contains the transformation code for converting the table cell, row or whole table to the mentioned object. This takes the place of XStream conversion but now we also need to take care of table data conversion to objects which were earlier automatic.

Let us look at some Cucumber 2 code for converting a DataTable into a list of objects.

Refer to [LecturePrimitive](https://github.com/grasshopper7/cuke2-parameter-datatable/blob/master/cuke2-parameter-datatable/src/test/java/dataobject/LecturePrimitive.java) for relevant code.

	Given the list primitive lecture details are
    | profName | size | profLevel  |
    | Jane     |   40 | Assistant  |
    | Doe      |   30 | Associate  |

    @Given("the list primitive lecture details are")
    public void thePrimitiveLectureDetailsAre(List<LecturePrimitive> lectures) {
      //Returns a list of LecturePrimitive objects
    }

Now if we use this same piece of code in Cucumber 3 we will get this error.

	cucumber.runtime.CucumberException: Could not convert arguments for step [the list primitive lecture details are] defined at ......... It appears you did not register a data table type 

Let us look at Cucumber 3 code by adding a DataTableType for conversion in the same configureTypeRegistry() method where the ParameterType is defined.

	registry.defineDataTableType(new DataTableType(LecturePrimitive.class, new TableEntryTransformer<LecturePrimitive>() {
		@Override
		public LecturePrimitive transform(Map<String, String> entry) {
			return LecturePrimitive.createLecture(entry);
		}
	}));
	
This will now output the same result as in the earlier cucumber versions.

Let us look at DataTableType constructor in more detail. TableTransformer is just a placeholder for specific transformer; it is not an interface or abstract class.

	DataTableType
	LecturePrimitive.class, -> Desired object class
	TableTransformer -> Transformation code

There are 4 types of TableTransformer - TableEntryTransformer, TableRowTransformer, TableCellTransformer, TableTransformer.

	Transformer Type		Parameter passed to transform()		Usage scenarios
	TableEntryTransformer		Map<String, String>			Transform DataTable containing header
	TableRowTransformer		List<String>				Transform DataTable without header
	TableCellTransformer		String					Transform a single cell into object
	TableTransformer		DataTable				Transform a whole table


DataTable  List of list of primitives – There is no need to write code for Cucumber 2 or Cucumber 3. This will be handled automatically.

DataTable  List of Object with primitive fields – This is the case mentioned above. To repeat, no need to write code for Cucumber 2. In Cucumber 3, a custom transformer has to be written as a DataTableType.
DataTable  List of Object with primitive and enum fields – In Cucumber 2 this will be handled automatically. In Cucumber 3, the custom transformer will need to mention the code to wire the enum field in the object.
Refer to LecturePrimitiveEnum
public enum ProfLevels {  ASSISTANT, ASSOCIATE, PROFESSOR	}
public class LecturePrimitiveEnum {
	private String profName;	
	private int size;		
	private ProfLevels profLevel;
	//Getter setter methods

public static LecturePrimitiveEnum createLecture(Map<String, String> entry) {
		LecturePrimitiveEnum lecture = new LecturePrimitiveEnum();
		lecture.setProfName(entry.get("profName"));
		lecture.setSize(Integer.parseInt(entry.get("size")));
lecture.setProfLevel(ProfLevels.valueOf(entry.get("profLevel").toUpperCase()));
	return lecture;
}
}

Scenario:
   Given the list primitive enum lecture details are
      | profName | size | profLevel  |
      | Jane     |   40 | Assistant  |
      | Doe      |   30 | Associate  |

@Given("the list primitive enum lecture details are")
public void thePrimitiveEnumLectureDetailsAre(List<LecturePrimitiveEnum> lectures) {
	//Returns a list of LecturePrimitiveEnum objects
}

DataTable  List of Object with other objects as fields – This is where things get interesting. In Cucumber 2, one could write the code in the stepdefinition method or work with XStream to convert to the object.

Refer to Professor and  ProfessorXStreamConverter
Refer to Topic and TopicXStreamConverter
Refer to Rooms and RoomsXStreamConverter

Scenario: XStream datatable scenario List<Lecture>
  Given the list lecture details are
     | profName | topic         | size | frequency | rooms     |
     | Jack     | A1:Topic One  |   40 |         3 | 101A,302C |
     | Daniels  | B5:Topic Five |   30 |         2 | 220E,419D |


@Given("^the lecture details are$")
public void theLectureDetailsAre(List<Lecture> lectures) {
	//List of Lecture objects
}

//Global registration on runner
@XStreamConverters({
	@XStreamConverter(value = ProfessorXStreamConverter.class),
	@XStreamConverter(value = RoomsXStreamConverter.class),
	@XStreamConverter(value = TopicXStreamConverter.class)
	})
@RunWith(Cucumber.class)

public class Lecture {
	private Professor profName;
	private Topic topic;
	private int size;
	private int frequency;
	private Rooms rooms;
	//Getter setter methods
}

public class Professor {
	private String profName;
}

public class ProfessorXStreamConverter extends AbstractSingleValueConverter implements Converter {

	@Override
	public boolean canConvert(Class cls) {
		return Professor.class.isAssignableFrom(cls);}

	@Override
	public Object fromString(String inputName) {		
		return Professor.parseProfessor(inputName);}
	
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.setValue(((Professor) value).toString());}

	@Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		return Professor.parseProfessor(reader.getValue());}
}

public class Topic {	
	private String code;	
	private String name;

public static Topic parseTopic(String top) {
		String[] topicData = top.split(":");
		Topic topic = new Topic(topicData[0],topicData[1]);		
		return topic;
	}
}

public class TopicXStreamConverter implements Converter {

	@Override
	public boolean canConvert(Class cls) {
		return Topic.class.isAssignableFrom(cls);	}

	
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.setValue(((Topic) value).toString());    }

	@Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		return Topic.parseTopic(reader.getValue());    }
}

public class Rooms {
	private List<Room> rooms;

public static Rooms parseRooms(String rooms) {
		return new Rooms( Arrays.stream(rooms.split(",")).map(Room::new).collect(Collectors.toList()));
	}
}

public class Room {
	private String roomNumber;
}

public class RoomsXStreamConverter implements Converter {

	@Override
	public boolean canConvert(Class cls) {
		//return cls.equals(Rooms.class);
		return Rooms.class.isAssignableFrom(cls);	}

	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        //Rooms  rooms = (Rooms) value;
        writer.setValue(((Rooms) value).toString());    }

	@Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		return Rooms.parseRooms(reader.getValue());    }
}

In Cucumber 3 this is easier to accomplish, at least the amount of code is reduced. The scenario, step definition and dataobjects remain the same.  All we need to do is to register a new DataTableType.

Refer to Lecture

registry.defineDataTableType(new DataTableType(Lecture.class, new TableEntryTransformer<Lecture>() {
	@Override
	public Lecture transform(Map<String, String> entry) {
		return Lecture.createLecture(entry);
	}
}));

public class Lecture {
	private Professor profName;
	private Topic topic;
	private int size;
	private int frequency;
	private Rooms rooms;
	//Getter setter methods

public static Lecture createLecture(Map<String, String> entry) {
		Lecture lecture = new Lecture();
		lecture.setProfName(new Professor(entry.get("profName")));
		lecture.setSize(Integer.parseInt(entry.get("size")));
		lecture.setFrequency(Integer.parseInt(entry.get("frequency")));
		lecture.setRooms(Rooms.parseRooms(entry.get("rooms")));
		lecture.setTopic(Topic.parseTopic(entry.get("topic")));
		return lecture;
	}
}

DataTable without header  List of Object (or any other collection) - In Cucumber 2, the way out is to accept a List<List<String>> as a parameter to the stepdefinition method and write the conversion code to the desired collection. 

Refer to LectureLite

  Scenario: XStream datatable scenario List<LectureLite>
    Given the list no header lecture details are
      | John Doe | A1:Topic One  | 40 | 3 | 101A,302C |
      | Jane Doe | B5:Topic Five | 30 | 2 | 220E,419D |

public void theListNoHeaderLectureDetailsAre(List<List<String>> lectstrs) {
	List<LectureLite> lectlite = new ArrayList<>();
	for(List<String> row : lectstrs)
		lectlite.add(LectureLite.createLectureLite(row));		
}

public static LectureLite createLectureLite(List<String> row) {
	LectureLite lecture = new LectureLite();
	Professor prof = new Professor();
	prof.setProfName(row.get(0));
	lecture.setProfName(prof);
	lecture.setTopic(Topic.parseTopic(row.get(1)));
	lecture.setSize(Integer.parseInt(row.get(2)));
	lecture.setFrequency(Integer.parseInt(row.get(3)));
	lecture.setRooms(Rooms.parseRooms(row.get(4)));
	return lecture;
}

In Cucumber 3, we need to add a DataTableType with the TableRowTransformer for conversion which handles DataTable without headers. We are using LectureLite in place of Lecture as the same class cannot be registered with different transformers.

Refer to LectureLite

@Given("the list no header lecture details are")
public void theListNoHeaderLectureDetailsAre(List<LectureLite> lectures) {	
}

registry.defineDataTableType(new DataTableType(LectureLite.class, new TableRowTransformer<LectureLite>() {
	@Override
	public LectureLite transform(List<String> row) throws Throwable {
		return LectureLite.createLectureLite(row);
	}
}));

public static LectureLite createLectureLite(List<String> row) {
	LectureLite lecture = new LectureLite();
	lecture.setProfName(new Professor(row.get(0)));
	lecture.setTopic(Topic.parseTopic(row.get(1)));
	lecture.setSize(Integer.parseInt(row.get(2)));
	lecture.setFrequency(Integer.parseInt(row.get(3)));
	lecture.setRooms(Rooms.parseRooms(row.get(4)));
	return lecture;
}

DataTable with 2 columns  Map with primitive key and value – There is no need to write code for Cucumber 2 or Cucumber 3. This will be handled automatically.

DataTable with 2 columns  Map with custom object key and value – In Cucumber 2, XStream will automatically convert it using the single argument constructor or we can set up a XStream converter. In Cucumber 3, we need to register new DataTableType.

DataTable with more than 2 columns  Map with primitive key and custom object value – In Cucumber 2, automatic conversion only works for 2 columns. The stepdefinition method needs to accept a List<List<String>> as a parameter and write the conversion code to a Map. Notice that the DataTable does not have headers.

Refer to Lecture

Scenario: XStream datatable scenario Map<String, Lecture>
    Given the map primitive key lecture details are
      | 1 | John Doe | A1:Topic One  | 40 | 3 | 101A,302C |
      | 2 | Jane Doe | B5:Topic Five | 30 | 2 | 220E,419D |

@Given("the map primitive key lecture details are")
public void theMapPrimitiveKey(List<List<String>> lectstrs) {
	Map<String, Lecture> lects = new HashMap<>();
	for(List<String> row : lectstrs)
lects.put(row.get(0), Lecture.createLecture(row.subList(1,row.size())));
}

public static Lecture createLecture(List<String> row) {
	Lecture lecture = new Lecture();
	Professor prof = new Professor();
	prof.setProfName(row.get(0));
	lecture.setProfName(prof);
	lecture.setTopic(Topic.parseTopic(row.get(1)));
	lecture.setSize(Integer.parseInt(row.get(2)));
	lecture.setFrequency(Integer.parseInt(row.get(3)));
	lecture.setRooms(Rooms.parseRooms(row.get(4)));
	return lecture;
}

In Cucumber 3, this is handled automatically with the registration of a new DataTableType. Only requirement for this automatic conversion to a Map is the first column of the DataTable will be considered as the key if there is no header. This will be the first row if the @Transpose annotation is used for a table with vertical headers.

Refer to Lecture

Scenario: XStream datatable scenario Map<String, Lecture>
    Given the map primitive key lecture details are
      |   | profName | topic         | size | frequency | rooms     |
      | 1 | John Doe | A1:Topic One  |   40 |         3 | 101A,302C |
      | 2 | Jane Doe | B5:Topic Five |   30 |         2 | 220E,419D |

@Given("the map primitive key lecture details are")
public void theMapPrimitiveKey(Map<String,Lecture> lectures) {
}

registry.defineDataTableType(new DataTableType(Lecture.class, new TableEntryTransformer<Lecture>() {
			@Override
			public Lecture transform(Map<String, String> entry) {
				return Lecture.createLecture(entry);
			}
		}));

public static Lecture createLecture(Map<String, String> entry) {
	Lecture lecture = new Lecture();
	lecture.setProfName(new Professor(entry.get("profName")));
	lecture.setSize(Integer.parseInt(entry.get("size")));
	lecture.setFrequency(Integer.parseInt(entry.get("frequency")));
	lecture.setRooms(Rooms.parseRooms(entry.get("rooms")));
	lecture.setTopic(Topic.parseTopic(entry.get("topic")));
	return lecture;
}

DataTable with more than 2 columns  Map with custom object key value – In Cucumber 2, automatic conversion only works for 2 columns. This is similar to the above transformation. The stepdefinition method needs to accept a List<List<String>> as a parameter and write the conversion code to a Map.  The scenario is similar as above.

@Given("the map lecture details are")
public void theMapLectureDetailsAre(List<List<String>> lectstrs) {
	mapIdLecture(lectstrs);
}

private Map<LectureId, Lecture> mapIdLecture(List<List<String>> lectstrs) {
	Map<LectureId, Lecture> lects = new HashMap<>();
	for(List<String> row : lectstrs)
lects.put(new LectureId(Integer.parseInt(row.get(0))), Lecture.createLecture(row.subList(1, row.size())));
	return lects;
}

In Cucumber 3, we need to add an additional DataTableType for converting the key to the object using the TableCellTransformer. We have already defined the converter for the value object. The scenario is similar as above.  The conversion of Lecture value is same as in the previous transformation.

@Given("the map lecture details are")
public void theMapLectureDetailsAre(Map<LectureId, Lecture> lectures) {
}

registry.defineDataTableType(new DataTableType(LectureId.class, new TableCellTransformer<LectureId>() {
	@Override
	public LectureId transform(String cell) throws Throwable {
		return new LectureId(Integer.parseInt(cell));
	}
}));

DataTable  Single Object – In Cucumber 2, the stepdefinition method needs to accept a List<List<String>> or DatatTable as a parameter and write the conversion code to the desired object.

Scenario: XStream datatable scenario Lectures
    Given all lectures details
      | profName | topic          | size | frequency | rooms     |
      | John     | A1:Topic One   |   40 |         3 | 101A,302C |
      | Jane     | Z9:Topic Six   |   30 |         2 | 220E,419D |  
	
@Given("all lectures details")
public void allLecturesDetails(List<List<String>> lectstrs) {
	Lectures lectures = new Lectures();
	for(List<String> row : lectstrs)
		lectures.addLecture(Lecture.createLecture(row));
}

public class Lectures {
	private List<Lecture> lectures;
}

In Cucumber 3, we need to add a new DataTableType with the TableTransformer for conversion of DataTable to the desired class.

Scenario: XStream datatable scenario Lectures
    Given all lectures details
      | profName | topic          | size | frequency | rooms     |
      | John     | A1:Topic One   |   40 |         3 | 101A,302C |
      | Jane     | Z9:Topic Six   |   30 |         2 | 220E,419D |

@Given("all lectures details")
public void allLecturesDetails(Lectures lectures) {
}

registry.defineDataTableType(new DataTableType(Lectures.class, new TableTransformer<Lectures>() {
	@Override
	public Lectures transform(DataTable table) throws Throwable {
List<Lecture> lects = table.asMaps().stream().map(m -> Lecture.createLecture(m)).collect(Collectors.toList());
		return new Lectures(lects);
	}
}));

Transpose DataTable  Converting to a list or map – If the headers are switched from horizontal to vertical that is, the table is transposed, add the @Transpose annotation in the stepdefinition method. 

