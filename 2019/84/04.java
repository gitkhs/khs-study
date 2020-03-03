// Check Examples

import java.time.*;
import java.util.*;
import java.util.function.Supplier;

//Compiler version JDK 11.0.2

class s84_4 {  
	public static void main(String[] args)  {
		System.out.println("Composite 패턴");
		CompositeTask root = new CompositeTask("root", LocalDateTime.now());
		root.addTask("sub1", LocalDateTime.now());
		root.addTask("sub2", LocalDateTime.now());

		TaskReport report = root.getReport(CompositeSortType.TITLE_ASC);
		List<TaskReport> list = report.getList();
		CompositeTask sub1 = list.get(0).getTask();
		CompositeTask sub2 = list.get(1).getTask();
		
		sub1.addTask("sub1_1", LocalDateTime.now());
		sub2.addTask("sub2_1", LocalDateTime.now());
		sub2.addTask("sub2_2", LocalDateTime.now());

		System.out.println("Visitor 패턴");

		Renderer render = new Renderer(()-> new ConsoleVisitor());
		render.render(root.getReport(CompositeSortType.TITLE_ASC));

		Renderer render2 = new Renderer(()-> new JsonVisitor());
		render2.render(root.getReport(CompositeSortType.TITLE_ASC));
	}
}


//==============================//
enum CompositeSortType {
	TITLE_DESC {
		@Override
		int compare(CompositeTask a, CompositeTask b) {
			return a.getTitle().compareTo(b.getTitle());
		}
	},
	TITLE_ASC {
		@Override
		int compare(CompositeTask a, CompositeTask b) {
			return b.getTitle().compareTo(a.getTitle());
		}
		
	},
	DATE_DESC {
		@Override
		int compare(CompositeTask a, CompositeTask b) {
			return a.getDate().compareTo(b.getDate());
		}
	},
	DATE_ASC {
		@Override
		int compare(CompositeTask a, CompositeTask b) {
			return b.getDate().compareTo(a.getDate());
		}
	};
	
	abstract int compare(CompositeTask a, CompositeTask b);
}

//==============================//
class CompositeTask {
	private String title;
	private LocalDateTime date;
	private Boolean isComplate = false;
	private final Set<CompositeTask> list = new HashSet<>();
	CompositeTask(String title, LocalDateTime date) {
		setTitle(title);
		setDate(date);
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	public LocalDateTime getDate() {
		return date;
	}
	public Boolean isComplate() {
		return isComplate;
	}
	public void toggle() {
		isComplate = !isComplate;
	}

	public void addTask(String title, LocalDateTime date) {
		list.add(new CompositeTask(title, date));
	}
	public void removeTask(CompositeTask task) {
		list.remove(task);
	}
	public TaskReport getReport(CompositeSortType type) {
		TaskReport report = new TaskReport(this);
		for(CompositeTask t:list) report.add(t.getReport(type));
		return report;
	}
}

//==============================//
class TaskReport {
	private final CompositeTask task;
	private final List<TaskReport> list = new ArrayList<>();
	TaskReport(CompositeTask task) {
		this.task = task;
	}
	public CompositeTask getTask() {
		return task;
	}
	public void add(TaskReport report) {
		list.add(report);
	}
	public List<TaskReport> getList() {
		return list;
	}
}

//==============================//
class Renderer {
	private final Supplier<Visitor> factory;
	public Renderer(Supplier<Visitor> factory) {
		this.factory = factory;
	}
	
	public void render(TaskReport report) {
		render(factory.get(), report, 0);
	}
	private void render(Visitor visitor, TaskReport report, int depth) {
		visitor.drawTask(report.getTask(), depth);
		for(TaskReport r:report.getList()) render(visitor, r, depth+1);
		visitor.end(depth);
	}
}

//==============================//
interface Visitor {
	void drawTask(CompositeTask task, int depth);
	void end(int depth);
}

//==============================//
class ConsoleVisitor implements Visitor {
	@Override
	public void drawTask(CompositeTask task, int depth) {
		String padding = "";
		for(int i=0; i<depth; i++) padding += "-";
		
		System.out.println(padding + (task.isComplate() ? "[v] " : "[ ] ")
			+ task.getTitle() + "(" + task.getDate() + ")");
	}
	
	@Override
	public void end(int depth) {
		
	}
}

//==============================//
class JsonVisitor implements Visitor {
	@Override
	public void drawTask(CompositeTask task, int depth) {
		String padding = getPadding(depth);
		System.out.println(padding + "{");
		System.out.println(padding + "\ttitle: \"" + task.getTitle() + "\",");
		System.out.println(padding + "\tdate: \"" + task.getDate() + "\",");
		System.out.println(padding + "\tisComplate: \"" + task.isComplate() + "\",");
		System.out.println(padding + "\tsub: [");
	}
	@Override
	public void end(int depth) {
		String padding = getPadding(depth);
		System.out.println(padding + "\t]");
		System.out.println(padding + "},");
	}
	private String getPadding(int depth) {
		String padding = "";
		for(int i=0; i<depth; i++) padding += "\t";
		return padding;
	}
}
