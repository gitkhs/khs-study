// Check Examples

import java.time.*;
import java.util.*;
import java.util.function.Supplier;

//Compiler version JDK 11.0.2

class s84_5 {  
	public static void main(String[] args)  {
		System.out.println("Composite 패턴");
		CommandTask root = new CommandTask("root", LocalDateTime.now());
		root.addTask("sub1", LocalDateTime.now());
		root.addTask("sub2", LocalDateTime.now());

		Renderer render = new Renderer(()-> new ConsoleVisitor());
		render.render(root.getReport(CompositeSortType.TITLE_ASC));

//        root.undo();
//        renderer1.render(root.getReport(CompositeSortType.TITLE_ASC));
//        root.undo();
//        renderer1.render(root.getReport(CompositeSortType.TITLE_ASC));
//        root.redo();
//        renderer1.render(root.getReport(CompositeSortType.TITLE_ASC));

//        TaskReport report = root.getReport(CompositeSortType.TITLE_ASC);
//        List<TaskReport> list = report.getList();
//        CompositeTask sub1 = list.get(0).getTask();
//        CompositeTask sub2 = list.get(1).getTask();
//        sub1.addTask("sub1_1", LocalDateTime.now());
//        sub1.addTask("sub1_2", LocalDateTime.now());
//        sub2.addTask("sub2_1", LocalDateTime.now());
//        sub2.addTask("sub2_2", LocalDateTime.now());

//        Renderer renderer2 = new Renderer(()->new JsonVisitor());
//        renderer2.render(root.getReport(CompositeSortType.TITLE_ASC));
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

	public CompositeTask addTask(String title, LocalDateTime date) {
		CompositeTask task = new CompositeTask(title, date);
		list.add(task);
		return task;
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
		render(factory.get(), report, 0, true);
	}
	private void render(Visitor visitor, TaskReport report, int depth, boolean isEnd) {
		visitor.drawTask(report.getTask(), depth);
		List<TaskReport> subList = report.getList();
		int i = subList.size();
		for (TaskReport r : subList) render(visitor, r, depth + 1, --i == 0);
		visitor.end(depth, isEnd);
	}
}

//==============================//
interface Visitor {
	void drawTask(CompositeTask task, int depth);
	void end(int depth, boolean isEnd);
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
	public void end(int depth, boolean isEnd) {
		
	}
}

//==============================//
class JsonVisitor implements Visitor {
	private String result = "";
	
	@Override
	public void drawTask(CompositeTask task, int depth) {
		result += "{";
		result += "  title: \"" + task.getTitle() + "\",";
		result += "  date: \"" + task.getDate() + "\",";
		result += "  isComplete: " + task.isComplate() + ",";
		result += "  sub: [ ";
	}
	@Override
	public void end(int depth, boolean isEnd) {
		result += "  ]";
		result += "}";
		if (!isEnd) result += ",";
	}
	
	public String getJson() {
		return result;
	}
}


// ============================== //
interface Command {
	void execute(CompositeTask task);
	void undo(CompositeTask task);
}
// ============================== //
class CommandTask {
	private final CompositeTask task;
	private List<Command> commands = new ArrayList<>();
	private int cursor = 0;
	private final Map<String, String> saved = new HashMap<>();
	CommandTask(String title, LocalDateTime date) {
		task = new CompositeTask(title, date);
	}

	public void save(String key) {
		JsonVisitor visitor = new JsonVisitor();
		Renderer renderer1 = new Renderer(()->visitor);
		renderer1.render(task.getReport(CompositeSortType.TITLE_ASC));
		saved.put(key, visitor.getJson());
	}
	public void load(String key) {
		String json = saved.get(key);
		// subTask 삭제
		// json 순회하며 복원
	}
	public void redo() {
		if (cursor == commands.size() - 1) return;
		commands.get(++cursor).execute(task);
	}
	public void undo() {
		if (cursor < 0) return;
		commands.get(cursor--).undo(task);
	}
	private void addCommand(Command cmd) {
		for (int i = commands.size() - 1; i > cursor; i--) {
			commands.remove(i);
		}
		cmd.execute(task);
		commands.add(cmd);
		cursor = commands.size() - 1;
	}
	
	public void toggle() {
		addCommand(new Toggle());
	}
	public void setTitle(String title) {
		addCommand(new Title(title));
	}
	public void setDate(LocalDateTime date) {
		addCommand(new Date(date));
	}
	public String getTitle() {
		return task.getTitle();
	}
	public LocalDateTime getDate() {
		return task.getDate();
	}
	public TaskReport getReport(CompositeSortType type) {
		return task.getReport(type);
	}
	public void addTask(String title, LocalDateTime date) {
		addCommand(new Add(title, date));
	}
	public void removeTask(CompositeTask task) {
		addCommand(new Remove(task));
	}
}
// ============================== //
class Title implements Command {
	private final String title;
	private String oldTitle;
	public Title(String title) {
		this.title = title;
	}
	
	@Override
	public void execute(CompositeTask task) {
		oldTitle = task.getTitle();
		task.setTitle(title);
	}
	@Override
	public void undo(CompositeTask task) {
		task.setTitle(oldTitle);
	}
}
// ============================== //
class Date implements Command {
	private final LocalDateTime date;
	private LocalDateTime oldDate;
	public Date(LocalDateTime date) {
		this.date = date;
	}
	@Override
	public void execute(CompositeTask task) {
		oldDate = task.getDate();
		task.setDate(date);
	}
	@Override
	public void undo(CompositeTask task) {
		task.setDate(oldDate);
	}
}
// ============================== //
class Toggle implements Command {
	@Override
	public void execute(CompositeTask task) {
		task.toggle();
	}
	@Override
	public void undo(CompositeTask task) {
		task.toggle();
	}
}
// ============================== //
class Add implements Command {
	private final String title;
	private final LocalDateTime date;
	private CompositeTask oldTask;
	public Add(String title, LocalDateTime date) {
		this.title = title;
		this.date = date;
	}
	
	@Override
	public void execute(CompositeTask task) {
		oldTask = task.addTask(title, date);
	}
	@Override
	public void undo(CompositeTask task) {
		task.removeTask(oldTask);
	}
}
// ============================== //
class Remove implements Command {
	private final CompositeTask baseTask;
	private String oldTitle;
	private LocalDateTime oldDate;
	public Remove(CompositeTask task) {
		this.baseTask = task;
	}
	
	@Override
	public void execute(CompositeTask task) {
		oldTitle = task.getTitle();
		oldDate = task.getDate();
		task.removeTask(baseTask);
	}
	@Override
	public void undo(CompositeTask task) {
		task.addTask(oldTitle, oldDate);
	}
}