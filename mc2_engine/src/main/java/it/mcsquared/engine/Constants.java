package it.mcsquared.engine;

public abstract class Constants {
	public static final String MAIN_PAGE = "/main.html";
	public static final String LOGIN_PAGE = "/login.html";
	public static final String ERROR_PAGE = "/resources/default/no-service.html";
	public static final String NOT_FOUND_PAGE = "/resources/default/404.html";
	public static final String CONTENT = "content";
	public static final String CONTENT_PREFIX = "/WEB-INF/" + CONTENT;

	public interface InitParams {
		public static final String CONTROLLERS_PACKAGE = "controllers.package";
		public static final String PROVIDERS_PACKAGE = "providers.package";
		public static final String CONF_DIR = "conf.dir";
	}

	public interface ApplicationAttributes {
		public static final String APP_LABELS_MANAGER = "appLabels";
		public static final String ONE_PERMANENT_SESSION = "ONE_PERMANENT_SESSION";
	}

	public interface SessionAttributes {
		public static final String LABELS_MANAGER = "labels";

		public static final String SESSION_USER = "currentUser";
		public static final String FIRM_ID = "FIRM_ID";
		public static final String CURRENT_CONTROLLER = "CURRENT_CONTROLLER";

		public static final String CURRENT_USER = "currentUser";
		// public static final String EXTERNAL_USER = "EXTERNAL_USER";
		//
		// public static final String DOWNLOAD_FILE = "DOWNLOAD_FILE";
		// public static final String DOWNLOAD_FILE_NAME = "DOWNLOAD_FILE_NAME";
		// public static final String DOWNLOAD_FILE_CONTENT_TYPE = "DOWNLOAD_FILE_CONTENT_TYPE";
		//
		// public static final String UPLOADED_FILE = "UPLOADED_FILE";

	}

	public interface ReqParams {
		public static final String COMMAND = "command";
		public static final String TABLE_DATA = "table-data";
	}

}
