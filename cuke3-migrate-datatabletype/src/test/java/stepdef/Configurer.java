package stepdef;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import cucumber.api.TypeRegistry;
import cucumber.api.TypeRegistryConfigurer;
import dataobject.Lecture;
import dataobject.LectureId;
import dataobject.LectureLite;
import dataobject.LecturePrimitive;
import dataobject.LecturePrimitiveEnum;
import dataobject.Lectures;
import dataobject.Professor;
import dataobject.ProfLevels;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellTransformer;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.datatable.TableRowTransformer;
import io.cucumber.datatable.TableTransformer;

public class Configurer implements TypeRegistryConfigurer {

	@Override
	public void configureTypeRegistry(TypeRegistry registry) {
		
		registry.defineDataTableType(new DataTableType(Lecture.class, new TableEntryTransformer<Lecture>() {
			@Override
			public Lecture transform(Map<String, String> entry) {
				return Lecture.createLecture(entry);
			}
		}));
		
		registry.defineDataTableType(new DataTableType(LecturePrimitive.class, new TableEntryTransformer<LecturePrimitive>() {
			@Override
			public LecturePrimitive transform(Map<String, String> entry) {
				return LecturePrimitive.createLecture(entry);
			}
		}));
		
		registry.defineDataTableType(new DataTableType(LecturePrimitiveEnum.class, new TableEntryTransformer<LecturePrimitiveEnum>() {
			@Override
			public LecturePrimitiveEnum transform(Map<String, String> entry) {
				return LecturePrimitiveEnum.createLecture(entry);
			}
		}));

		registry.defineDataTableType(new DataTableType(LectureId.class, new TableCellTransformer<LectureId>() {
			@Override
			public LectureId transform(String cell) throws Throwable {
				return new LectureId(Integer.parseInt(cell));
			}
		}));
		
		registry.defineDataTableType(new DataTableType(Professor.class, new TableCellTransformer<Professor>() {
			@Override
			public Professor transform(String cell) throws Throwable {
				return new Professor(cell);
			}
		}));
		
		registry.defineDataTableType(new DataTableType(ProfLevels.class, new TableCellTransformer<ProfLevels>() {
			@Override
			public ProfLevels transform(String cell) throws Throwable {
				return ProfLevels.valueOf(cell.toUpperCase());
			}
		}));
		
		registry.defineDataTableType(new DataTableType(LectureLite.class, new TableRowTransformer<LectureLite>() {
			@Override
			public LectureLite transform(List<String> row) throws Throwable {
				return LectureLite.createLectureLite(row);
			}
		}));
		
		registry.defineDataTableType(new DataTableType(Lectures.class, new TableTransformer<Lectures>() {
			@Override
			public Lectures transform(DataTable table) throws Throwable {
				List<Lecture> lects = table.asMaps().stream().map(m -> Lecture.createLecture(m)).collect(Collectors.toList());
				return new Lectures(lects);
			}
		}));
		
	}

	@Override
	public Locale locale() {
		return Locale.ENGLISH;
	}

}
