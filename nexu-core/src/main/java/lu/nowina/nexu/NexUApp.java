/**
 * © Nowina Solutions, 2015-2015
 *
 * Concédée sous licence EUPL, version 1.1 ou – dès leur approbation par la Commission européenne - versions ultérieures de l’EUPL (la «Licence»).
 * Vous ne pouvez utiliser la présente œuvre que conformément à la Licence.
 * Vous pouvez obtenir une copie de la Licence à l’adresse suivante:
 *
 * http://ec.europa.eu/idabc/eupl5
 *
 * Sauf obligation légale ou contractuelle écrite, le logiciel distribué sous la Licence est distribué «en l’état»,
 * SANS GARANTIES OU CONDITIONS QUELLES QU’ELLES SOIENT, expresses ou implicites.
 * Consultez la Licence pour les autorisations et les restrictions linguistiques spécifiques relevant de la Licence.
 */
package lu.nowina.nexu;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.token.PasswordInputCallback;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lu.nowina.nexu.api.EnvironmentInfo;
import lu.nowina.nexu.api.plugin.HttpPlugin;
import lu.nowina.nexu.api.plugin.SignaturePlugin;
import lu.nowina.nexu.flow.BasicFlowRegistry;
import lu.nowina.nexu.generic.DatabaseWebLoader;
import lu.nowina.nexu.generic.HttpDataLoader;
import lu.nowina.nexu.generic.SCDatabase;
import lu.nowina.nexu.generic.SCDatabaseLoader;
import lu.nowina.nexu.view.SystrayMenu;
import lu.nowina.nexu.view.core.OperationResult;
import lu.nowina.nexu.view.core.UIDisplay;
import lu.nowina.nexu.view.core.UIOperation;

public class NexUApp extends Application implements UIDisplay {

	private static final Logger logger = LoggerFactory.getLogger(NexUApp.class.getName());

	private Stage stage;

	private UIOperation<?> currentOperation;
	
	private AppConfig getConfig() {
		return NexuLauncher.getConfig();
	}

	private Properties getProperties() {
		return NexuLauncher.getProperties();
	}
	
	@Override
	public void start(Stage primaryStage) {
		Platform.setImplicitExit(false);

		this.stage = new Stage();

		try {

			File nexuHome = NexuLauncher.getNexuHome();
			SCDatabase db = null;
			if (nexuHome != null) {
				File store = new File(nexuHome, "store.xml");
				logger.info("Load database from " + store.getAbsolutePath());
				db = SCDatabaseLoader.load(store);
			} else {
				db = new SCDatabase();
			}

			UserPreferences prefs = new UserPreferences();
			CardDetector detector = new CardDetector(EnvironmentInfo.buildFromSystemProperties(System.getProperties()));

			DatabaseWebLoader loader = new DatabaseWebLoader(getConfig(), new HttpDataLoader());
			loader.start();

			InternalAPI api = new InternalAPI(this, prefs, db, detector, loader, new BasicFlowRegistry());

			for (String key : getProperties().stringPropertyNames()) {
				if (key.startsWith("plugin_")) {

					String pluginClassName = getProperties().getProperty(key);
					String pluginId = key.substring("plugin_".length());

					logger.info(" + Plugin " + pluginClassName);
					buildAndRegisterPlugin(api, pluginClassName, pluginId, false);

				}
			}

			new SystrayMenu(this, loader);

			logger.info("Start Jetty");

			startHttpServer(prefs, api);

			logger.info("Start finished");

		} catch (Exception e) {
			logger.error("Cannot start", e);
		}
	}

	private void startHttpServer(UserPreferences prefs, InternalAPI api) {
		new Thread(() -> {
			HttpServer server = buildHttpServer();
			server.setConfig(api, prefs, getConfig());
			try {
				server.start();
			} catch (Exception e) {
				logger.error("Cannot Jetty", e);
			}
		}).start();
	}

	/**
	 * Build the HTTP Server for the platform
	 * 
	 * @return
	 */
	private HttpServer buildHttpServer() {
		try {
			Class<HttpServer> cla = (Class<HttpServer>) Class.forName(getConfig().getHttpServerClass());
			logger.info("HttpServer is " + getConfig().getHttpServerClass());
			HttpServer server = cla.newInstance();
			return server;
		} catch (Exception e) {
			logger.error("Cannot instanciate Http Server " + getConfig().getHttpServerClass(), e);
			throw new RuntimeException("Cannot instanciate Http Server");
		}
	}

	private void buildAndRegisterPlugin(InternalAPI api, String pluginClassName, String pluginId, boolean exceptionOnFailure) {

		try {
			Class<?> clazz = Class.forName(pluginClassName);
			Object plugin = clazz.newInstance();
			for (Class<?> i : clazz.getInterfaces()) {
				registerPlugin(api, pluginId, i, plugin);
			}
		} catch (Exception e) {
			logger.error(MessageFormat.format("Cannot register plugin {0} (id: {1})", pluginClassName, pluginId), e);
			if (exceptionOnFailure) {
				throw new RuntimeException(e);
			}
		}

	}

	private void registerPlugin(InternalAPI api, String pluginId, Class<?> i, Object plugin) {
		if (SignaturePlugin.class.equals(i)) {
			SignaturePlugin p = (SignaturePlugin) plugin;
			p.init(pluginId, api);
		}
		if (HttpPlugin.class.equals(i)) {
			HttpPlugin p = (HttpPlugin) plugin;
			p.init(pluginId, api);
			api.registerHttpContext(pluginId, p);
		}
	}

	@Override
	public void display(Parent panel) {
		logger.info("Display " + panel + " in display " + this + " from Thread " + Thread.currentThread().getName());
		Platform.runLater(() -> {
			logger.info("Display " + panel + " in display " + this + " from Thread " + Thread.currentThread().getName());
			if (!stage.isShowing()) {
				stage = createStage();
				logger.info("Loading ui " + panel + " is a new Stage " + stage);
			} else {
				logger.info("Stage still showing, display " + panel);
			}
			stage.setScene(new Scene(panel, 300, 250));
			stage.show();
		});
	}

	private Stage createStage() {
		Stage newStage = new Stage();
		newStage.setAlwaysOnTop(true);
		newStage.setOnCloseRequest((e) -> {
			logger.info("Closing stage " + stage + " from " + Thread.currentThread().getName());
			stage.hide();
			e.consume();

			if(currentOperation != null) {
				currentOperation.signalUserCancel();
			}
			
		});
		return newStage;
	}

	@Override
	public void displayWaitingPane() {
	}

	@Override
	public void close() {

		Platform.runLater(() -> {
			Stage oldStage = stage;
			logger.info("Hide stage " + stage + " and create new stage");
			stage = createStage();
			oldStage.hide();
		});
		
	}

	@Override
	public void stop() throws Exception {
		logger.warn("Can only happen with explicite user request");
	}

	public <T extends Object> OperationResult<T> displayAndWaitUIOperation(String fxml, Object... params) {

		logger.info("Loading " + fxml + " view");
		FXMLLoader loader = new FXMLLoader();
		try {
			loader.load(getClass().getResourceAsStream(fxml));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Parent root = loader.getRoot();
		UIOperation<T> controller = loader.getController();

		display(root);
		return waitForUser(controller, params);
	}

	private <T> OperationResult<T> waitForUser(UIOperation<T> controller, Object... params) {
		try {
			logger.info("Wait on Thread " + Thread.currentThread().getName());
			controller.init(params);
			currentOperation = controller; 
			OperationResult<T> result = controller.waitEnd();
			currentOperation = null;
			displayWaitingPane();
			return result;
		} catch (InterruptedException e) {
			throw new RuntimeException();
		}
	}

	private final class FlowPasswordCallback implements PasswordInputCallback {
		@Override
		public char[] getPassword() {
			logger.info("Request password");
			OperationResult<char[]> passwordResult = displayAndWaitUIOperation("/fxml/password-input.fxml");
			return passwordResult.getResult();
		}
	}

	public PasswordInputCallback getPasswordInputCallback() {
		return new FlowPasswordCallback();
	}

}
