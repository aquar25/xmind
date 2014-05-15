package ru.codeanalyzer.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import ru.codeanalyzer.interfaces.ICData;

public class CData implements ICData {

	private class Item {
		public String value;
		public List<Item> items;

		public String get(String path) {
			String[] tree = path.split(",");

			try {

				List<Item> items = root.items.get(0).items;

				for (String node : tree) {
					int i = Integer.parseInt(node);
					Item item = items.get(i);
					if (item.items == null)
						return item.value;

					items = items.get(i).items;
				}

			} catch (Exception e) {

			}

			return "";
		}				
		
	}

	private Item root;
	private Type type;
	private String name;
	
	private void parse()	
	{
		int t = Integer.parseInt(root.get("0"));
		switch (t) {
		case 1:
			break;
		case 2:
			this.type = Type.Config;
			return;
		default:
			this.type = Type.NotObject;
			return;
		}

		t = Integer.parseInt(root.get("1,0"));
		switch (t) {
		case 19:
			if(!root.get("1,3").isEmpty())
			{
				this.type = Type.Enum;
				this.name = root.get("1,5,1,2");
			}else
			{
				this.type = Type.Report;
				this.name = root.get("1,3,1,2");
			}
			break;
		case 34:
			this.type = Type.Document;
			this.name = root.get("1,9,1,2");
			break;
		case 47:
			this.type = Type.Catalog;
			this.name = root.get("1,9,1,2");
			break;
		case 12:
			this.type = Type.CommonModules;
			this.name = root.get("1,1,2");
			break;
		case 16:
			this.type = Type.DataProcessor;
			this.name = root.get("1,3,1,2");
			break;
		default:
			this.type = Type.NotDefined;
			return;
		}
	}
	
	
	@Override
	public Type getType() {
		return type;				
	}

	@Override
	public void setText(String text) {
		// System.out.println(text);

		Stack<Item> stack = new Stack<Item>();

		root = new Item();
		root.value = "root";
		root.items = new ArrayList<Item>();

		StringBuilder buf = new StringBuilder();
		Item caret = null;
		Item parent = root;
		// List<Item> items = root.items;

		for (char c : text.toCharArray()) {
			switch (c) {
			case '{':

				caret = new Item();
				caret.value = "node";
				caret.items = new ArrayList<Item>();

				parent.items.add(caret);

				stack.push(parent);

				parent = caret;
				buf = new StringBuilder();
				break;
			case '}':

				if (!buf.toString().trim().isEmpty()) {
					caret = new Item();
					caret.value = buf.toString();
					parent.items.add(caret);
					buf = new StringBuilder();
				}

				parent = stack.pop();
				buf = new StringBuilder();
				break;

			case ',':

				if (buf.toString().trim().isEmpty())
					break;

				caret = new Item();
				caret.value = buf.toString();
				parent.items.add(caret);
				buf = new StringBuilder();

				break;
			default:
				buf.append(c);
				break;
			}
		}
		parse();
	}

	@Override
	public boolean needLoading() {
		
		return type!=Type.NotObject && 
				type!=Type.NotDefined &&
				type!=Type.Config;
	}

	@Override
	public String getName() {
		
		return name.replaceAll("\"", "");
	}
}
