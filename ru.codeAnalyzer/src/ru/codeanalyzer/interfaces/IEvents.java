package ru.codeanalyzer.interfaces;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.xmind.core.ITopic;

import ru.codeanalyzer.core.model.BuildInfo;



public interface IEvents {

	public enum topicRole {
		root, group1, group2, module, proc, hierarchy, param_list 		
	}
	
	public enum searchType {
		meta, text, proc		
	}
	
	public class CompareResults {
		public List<BuildInfo> equals = new ArrayList<BuildInfo>();
		public List<BuildInfo> added = new ArrayList<BuildInfo>();
		public List<BuildInfo> removed = new ArrayList<BuildInfo>();
		public List<BuildInfo> changed = new ArrayList<BuildInfo>();
	}
	
	public class searchTypeData {
		public int index;
		public String caption;
		public Image image;
		public String markerId;
		public searchTypeData(int index, String caption, Image image,
				String markerId) {
			super();
			this.index = index;
			this.caption = caption;
			this.image = image;
			this.markerId = markerId;
		}
		
	}
	
	
	searchTypeData searchTypeData(searchType type);

	void search(final searchType type, final String text,
			final ITopic selectedTopic);

	void searchByText();

	void openModule();

	void callHierarchy();

	void paramList();

	ITopic clear(boolean detached);

	void start();

	void openPerspective();

	boolean activeConfigLoadedCheck();

	boolean bothConfigLoadedCheck();

	List<BuildInfo> getCalled(BuildInfo data);

	List<BuildInfo> getCalls(BuildInfo data, boolean callsInObject);

	void goToProcedure(String line, BuildInfo context);

	void back();

	void next();

	void build();

	void compare();

	boolean nonActiveConfigLoadedCheck();

	void makeTopicWithProcLink(BuildInfo data);

	void toggleTopic();

}