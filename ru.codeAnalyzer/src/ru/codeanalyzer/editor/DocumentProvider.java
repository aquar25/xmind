package ru.codeanalyzer.editor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.StorageDocumentProvider;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import ru.codeanalyzer.editor.core.DocumentPartitionScanner;
import ru.codeanalyzer.editor.core.EditorInput;
import ru.codeanalyzer.utils.Const;

public class DocumentProvider extends StorageDocumentProvider {

	@Override
	public String getDefaultEncoding() {
		return Const.DEFAULT_CHARACTER_ENCODING;
	}

	@Override
	public String getEncoding(Object element) {
		return super.getEncoding(element);
	}

	@Override
	protected IAnnotationModel createAnnotationModel(Object element)
			throws CoreException {

		if (element instanceof EditorInput) {
			IResource resource = ResourcesPlugin.getWorkspace().getRoot();
			return new ResourceMarkerAnnotationModel(resource);
		}
//		if (element instanceof EditorInput) {
//			return new ProjectionAnnotationModel();
//		}
		
//		if (element instanceof EditorInput) {
//			IResource resource = (IResource) ((EditorInput)element).getAdapter(IResource.class);
//			return new ResourceMarkerAnnotationModel(resource);
//		}
		
		return super.createAnnotationModel(element);
	}

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
//		IDocument document = super.createDocument(element);

		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner =
				new FastPartitioner(
					new DocumentPartitionScanner(),
					new String[] {
						DocumentPartitionScanner.STRING});
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
		
	}

}
