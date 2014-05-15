package ru.codeanalyzer.interfaces;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;

/**
 * http://murygin.wordpress.com/2012/06/13/link-eclipse-view-to-editor/
 * @author Enikeev M.A.
 *
 */
public interface ILinkedWithEditorView {
	/**
	   * Called when an editor is activated
	   * e.g. by a click from the user.
	   * @param The activated editor part.
	   */
	  void editorActivated(IEditorPart activeEditor);
	 
	  /**
	   * @return The site for this view.
	   */
	  IViewSite getViewSite();

	void editorClosed();
}
