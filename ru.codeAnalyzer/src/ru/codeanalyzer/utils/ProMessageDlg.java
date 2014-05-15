package ru.codeanalyzer.utils;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

public class ProMessageDlg extends MessageDialog {

	public ProMessageDlg(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
	}

	@Override
	protected Control createCustomArea(Composite composite) {

		Link link = new Link(composite, SWT.WRAP | SWT.RIGHT);
		link.setText("<A>Получить pro</A>"); 
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {			
				Utils.OpenLink(Const.URL_proLinkOpen);
			}			
		});

		GridDataFactory
				.fillDefaults()
				.align(SWT.END, SWT.BEGINNING)
				.grab(true, false)
				.hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
						SWT.DEFAULT).applyTo(link);

		return composite;
	}

    public static boolean open(Shell parent, String title, String message)
    {
    	int style = SWT.NONE;
    	int kind = INFORMATION;

    	ProMessageDlg dialog = new ProMessageDlg(parent, title, null, message,
				kind,  new String[] { IDialogConstants.OK_LABEL }, 0);
    	style &= SWT.SHEET;
		dialog.setShellStyle(dialog.getShellStyle() | style);
		return dialog.open() == 0;

    }     
}