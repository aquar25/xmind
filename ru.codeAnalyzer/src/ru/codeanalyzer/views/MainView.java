package ru.codeanalyzer.views;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import ru.codeanalyzer.dialogs.ActivateDialog;
import ru.codeanalyzer.dialogs.EditDialog;
import ru.codeanalyzer.interfaces.IAuthorize;
import ru.codeanalyzer.interfaces.IDb;
import ru.codeanalyzer.interfaces.IDbManager;
import ru.codeanalyzer.interfaces.IEvents;
import ru.codeanalyzer.interfaces.IHistory;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.interfaces.IEvents.searchType;
import ru.codeanalyzer.interfaces.ILoaderService.operationType;
import ru.codeanalyzer.utils.Const;
import ru.codeanalyzer.utils.Utils;

public class MainView extends ViewPart {

	public static final String ID = "ru.codeanalyzer.views.mainView"; //$NON-NLS-1$
	private ActivateDialog activateDialog;
	private EditDialog editDialog;
	private Link lblInfo;
	private Button btnDb2;
	private Button btnDb1;
	private Text searchField;
	
	IDbManager dbManager = pico.get(IDbManager.class);
	IEvents events = pico.get(IEvents.class);
	private Combo searchCombo;
	private Button searchButton;
	private Button searchButton_1;
	private Button btnShow;
	private Button btnNewButton;
	private Button btnNewButton_1;
	public MainView() {
		dbManager.init();
				
	}

	@Override
	public void dispose() {
		
		super.dispose();
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		Button button;
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		{
			Group group = new Group(container, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
					4, 1));
			group.setLayout(new GridLayout(3, false));
			{
				lblInfo = new Link(group, SWT.NONE);
				lblInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
						false, 1, 1));
				lblInfo.setText("-");
				lblInfo.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Utils.OpenLink(Const.URL_download);
					}
				});
			}
			{
				button = new Button(group, SWT.FLAT);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						showLiscense();
					}
				});
				button.setImage(ResourceManager.getPluginImage(
						"ru.codeAnalyzer", "icons/refresh.png"));
				button.setToolTipText("Обновить");
			}
			{
				button = new Button(group, SWT.FLAT);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						openActivateDialog();
					}
				});
				button.setText("Активация");
			}
		}
		{
			Group group = new Group(container, SWT.NONE);
//			group.setText("Загрузка конфигураций");
			group.setLayout(new GridLayout(4, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

			//btnDb1 ****************************************************************
			{
				btnDb1 = new Button(group, SWT.FLAT | SWT.RADIO);
				btnDb1.setSelection(true);					
				btnDb1.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						radioDbOnClick(Const.DB1);
					}
				});
				btnDb1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				btnDb1.setText("Radio Button");
			}

			{
				button = new Button(group, SWT.FLAT);
				button.setToolTipText("Настройка");
				button.setImage(ResourceManager.getPluginImage(
						"ru.codeAnalyzer", "icons/edit.png"));
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						openEditDialog(Const.DB1, btnDb1);
					}
				});
			}
			{
				button = new Button(group, SWT.FLAT);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						btnExecuteOnClick(Const.DB1, btnDb1);
					}
				});
				button.setToolTipText("Выполнить операцию");
				button.setImage(ResourceManager.getPluginImage(
						"ru.codeAnalyzer", "icons/nav_go.gif"));
			}
			{
				button = new Button(group, SWT.FLAT);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						btnLoadFromDbOnClick(Const.DB1, btnDb1);
					}
				});
				button.setToolTipText("Загрузить конфигурацию из файла");
				button.setImage(ResourceManager.getPluginImage(
						"ru.codeAnalyzer", "icons/load.png"));
			}
			
			//btnDb2 ****************************************************************
			{
				btnDb2 = new Button(group, SWT.FLAT | SWT.RADIO);
				btnDb2.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						radioDbOnClick(Const.DB2);
					}
				});
				btnDb2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				button = new Button(group, SWT.FLAT);
				button.setToolTipText("Настройка");
				button.setImage(ResourceManager.getPluginImage(
						"ru.codeAnalyzer", "icons/edit.png"));
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						openEditDialog(Const.DB2, btnDb2);
					}
				});
			}

			{
				button = new Button(group, SWT.FLAT);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						btnExecuteOnClick(Const.DB2, btnDb2);
					}
				});
				button.setToolTipText("Выполнить операцию");
				button.setImage(ResourceManager.getPluginImage(
						"ru.codeAnalyzer", "icons/nav_go.gif"));
			}
			
			{
				button = new Button(group, SWT.FLAT);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						btnLoadFromDbOnClick(Const.DB2, btnDb2);
					}
				});
				button.setToolTipText("Загрузить конфигурацию из файла");
				button.setImage(ResourceManager.getPluginImage(
						"ru.codeAnalyzer", "icons/load.png"));
			}						

		}
		
				{
					btnShow = new Button(container, SWT.FLAT);
					btnShow.setFont(SWTResourceManager.getFont("Tahoma", 8, SWT.BOLD));
					btnShow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
					btnShow.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/markers/codeanalyzer-root.png"));
					btnShow.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							events.start();
						}
					});
					btnShow.setText("Показать конфигурацию");
				}
		Group group = new Group(container, SWT.NONE);
		group.setText("Поиск");
		group.setLayout(new GridLayout(4, false));
		GridData gd_group = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		gd_group.heightHint = 47;
		group.setLayoutData(gd_group);
		searchCombo = new Combo(group, SWT.READ_ONLY);
		searchCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchButton.setImage(events.searchTypeData(searchType.values()[searchCombo.getSelectionIndex()]).image);							
			}
		});
		
		searchCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		for(searchType type: searchType.values())
			searchCombo.add(events.searchTypeData(type).caption);
		searchCombo.select(0);
		{
			searchField = new Text(group, SWT.BORDER);
			searchField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.character=='\r')
						doSearch();
				}
			});
			searchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			searchButton = new Button(group, SWT.FLAT);
			searchButton.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/markers/codeanalyzer-search_text.png"));
			searchButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					doSearch();
				}
			});
			searchButton.setText("Найти");
			
			searchButton_1 = new Button(group, SWT.FLAT);			
			searchButton_1.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/fill.png"));
			searchButton_1.setToolTipText("Скопировать в окно процедур модуля");
			searchButton_1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					copySearch();
				}
			});
		}
		
//		{
//			group = new Group(container, SWT.NONE);
//			group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//			group.setLayout(new GridLayout(2, false));
			{
				btnNewButton = new Button(container, SWT.FLAT);
				btnNewButton.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/goto.gif"));
				btnNewButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						events.build();
					}
				});
				btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				btnNewButton.setText("Построить");
			}
			{
				btnNewButton = new Button(container, SWT.FLAT);
//				btnNewButton.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/goto.gif"));
				btnNewButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						events.compare();
					}
				});
				btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				btnNewButton.setText("Сравнить");
			}
			{
				btnNewButton_1 = new Button(container, SWT.FLAT);
				btnNewButton_1.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						pico.get(IHistory.class).clear();
						events.openPerspective();
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().resetPerspective();
					}
				});
				btnNewButton_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				btnNewButton_1.setText("Расположить окна по умолчанию");
			}
			{
				button = new Button(container, SWT.FLAT);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(getSite().getWorkbenchWindow().getShell(),"ru.codeanalyzer.preferences.PreferencePage", null, null);
						if (pref != null) pref.open();
					}
				});
				button.setToolTipText("Параметры");
				button.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/releng_gears.gif"));
			}	
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
//		}
		
		createActions();
		initializeToolBar();
		initializeMenu();

		setContents();
		
//		events.openPerspective();
		
	}

	protected void copySearch() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		if (page == null)
			return;
		
		try {
			
			OutlineView view = (OutlineView)page.showView(OutlineView.ID);
			view.copySearch(searchField.getText());
			
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	protected void doSearch() {
		events.search(searchType.values()[searchCombo.getSelectionIndex()], searchField.getText(), null);
		
	}

	protected void radioDbOnClick(String ID) {
		dbManager.setActive(ID);		
	}
	
	protected void btnExecuteOnClick(String ID, Button btn) {
		
		final IDb db = dbManager.get(ID);
		
		dbManager.execute(db);
					
		setDbContents(btn, ID);
	}

	protected void btnLoadFromDbOnClick(String ID, Button btn) {
		
		final IDb db = dbManager.get(ID);
		
		IPath path = Utils.browseFile(db.getDbPath(), getSite().getWorkbenchWindow().getShell(), "Выберите файл базы данных", "*.db");
		db.setDbPath(path.toString());
		db.setType(operationType.fromDb);
		db.save();
		dbManager.execute(db);

		setDbContents(btn, ID);
		
	}
	
	private void setContents() {
	
		showLiscense();
		
		setDbContents(btnDb1, Const.DB1);
		setDbContents(btnDb2, Const.DB2);
		
	}

	private void setDbContents(Button btn, String ID)
	{
		IDb info = dbManager.get(ID); 
		btn.setText(info.status());
		switch (info.getState()) {
		case Loaded:
			btn.setImage(ResourceManager.getPluginImage(
					"ru.codeAnalyzer", "icons/loaded_with_table.png"));
			break;

		case notLoaded:
			btn.setImage(ResourceManager.getPluginImage(
					"ru.codeAnalyzer", "icons/not_loaded.png"));
			break;
//		case LoadedWithLinkTable:
//			btn.setImage(ResourceManager.getPluginImage(
//					"ru.codeAnalyzer", "icons/loaded_with_table.png"));
//			break;
		default:
			btn.setImage(ResourceManager.getPluginImage(
					"ru.codeAnalyzer", "icons/not_loaded.png"));
			break;
		}	
	}
	
	protected void showLiscense() {
		lblInfo.setText(pico.get(IAuthorize.class).getInfo().ShortMessage());
		
	}

	protected void openActivateDialog() {
		
		activateDialog = new ActivateDialog(getSite().getWorkbenchWindow().getShell());
		activateDialog.open();
		
	}

	
	protected void openEditDialog(String ID, Button btn) {
		
		editDialog = new EditDialog(getSite().getWorkbenchWindow().getShell(), dbManager.get(ID));
		editDialog.open();	
		setDbContents(btn, ID);
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
//		IToolBarManager toolbarManager = getViewSite().getActionBars()
//				.getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
//		IMenuManager menuManager = getViewSite().getActionBars()
//				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
}
