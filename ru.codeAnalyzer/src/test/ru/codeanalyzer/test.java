package test.ru.codeanalyzer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;

public class test extends AbstractTopicDecoration {

	@Override
	protected void sketch(IFigure figure, Path shape, Rectangle box, int purpose) {
		 shape.addRectangle(box.x, box.y, box.width, box.height);
		 
		 shape.addRectangle(box.x, box.y, box.width/10, box.height);
		 
		 Color c = new Color(shape.getDevice(), 100, 100, 100);
		 setFillColor(figure, c);
//		 FontData fontData=new FontData();
//		 fontData.setHeight(12);
//		 fontData.setStyle(SWT.BOLD);
//		 fontData.setName("Arial black");//Monotype Corsiva");
//		  
//	     Font f = new Font(shape.getDevice(), fontData);
//	     
//		 shape.addString("документ", box.x, box.y - 15, f);

	}

	public test() {
		super();
	}

	public test(String id) {
		super(id);
	}

}
