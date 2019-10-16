package chat_ActiveMQ;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JMSChat extends Application{
	
	private MessageProducer messageProducer;
	private Session session;
	private MessageConsumer messageConsumer;
	private ConnectionFactory connectionFactory;
	private Connection connection;
	private ListView<String> listView;
	
	public static void main(String[] args) {
		Application.launch(JMSChat.class);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("JMS chat");
		BorderPane borderPane=new BorderPane();
		HBox hbox=new HBox();hbox.setPadding(new Insets(10));hbox.setSpacing(15);
		hbox.setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));
		
		Label label1=new Label("Code: ");
		TextField textField1=new TextField("C1");
		textField1.setPromptText("Code");
		
		Label label2=new Label("Host: ");
		TextField textField2=new TextField("localhost");
		textField2.setPromptText("Host");
		
		Label label3=new Label("Port: ");
		TextField textField3=new TextField("61616");
		textField3.setPromptText("Port");
		
		Button buttonConnecter=new Button("Connecter");
		
		hbox.getChildren().addAll(label1,textField1, label2,textField2, label3,textField3,buttonConnecter);
		
		borderPane.setTop(hbox);	
		
		VBox vbox=new VBox();
		GridPane gridPane=new GridPane();
		HBox hbox2=new HBox();
		vbox.getChildren().addAll(gridPane,hbox2);
		borderPane.setCenter(vbox);
		
		Label labelTo=new Label("To; ");
		TextField textFieldTo=new TextField("C1");
		
		Label labelMessage=new Label("Message: ");
		TextArea textMessage=new TextArea("Message");
		
		Button buttonEnvoyer=new Button("Envoyer");
		
		Label labelImage=new Label("Image: ");
		File file=new File("images");
		ObservableList<String> observableList=FXCollections.observableArrayList(file.list());
		ComboBox<String> comboBoxImage=new ComboBox<String>(observableList);
		comboBoxImage.getSelectionModel().select(0);
		Button buttonEnvoyerImage=new Button("Envoyer Image");
		
		ObservableList<String> observableListMessages=FXCollections.observableArrayList();
		listView=new ListView<String>(observableListMessages);
		
		File file2=new File("images/"+comboBoxImage.getSelectionModel().getSelectedItem());
		Image image=new Image(file2.toURI().toString());
		ImageView imageView=new ImageView(image);
		imageView.setFitWidth(400);imageView.setFitWidth(300);
		hbox2.getChildren().addAll(listView,imageView);
		hbox2.setPadding(new Insets(10)); hbox2.setSpacing(10);
		
		gridPane.setPadding(new Insets(10));
		textMessage.setPrefRowCount(2);
		gridPane.setVgap(10);gridPane.setHgap(10);
		textFieldTo.setPrefWidth(250);
		textMessage.setPrefWidth(250);
		gridPane.add(labelTo, 0, 0);gridPane.add(textFieldTo, 1, 0);
		gridPane.add(labelMessage, 0, 1);gridPane.add(textMessage, 1, 1);gridPane.add(buttonEnvoyer, 2, 1);
		gridPane.add(labelImage, 0, 2);gridPane.add(comboBoxImage, 1, 2);gridPane.add(buttonEnvoyerImage, 2, 2);
		
		Scene scene=new Scene(borderPane,900,650);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		buttonEnvoyer.setOnAction(e->{
			try {
				TextMessage textMsg=session.createTextMessage();
				textMsg.setText(textMessage.getText());
				textMsg.setStringProperty("code",textFieldTo.getText());
				messageProducer.send(textMsg);
			} catch (JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		buttonEnvoyerImage.setOnAction(e->{
			try {
				StreamMessage streamMessage=session.createStreamMessage();
				String imageName=comboBoxImage.getSelectionModel().getSelectedItem();
				File f=new File("images/"+imageName);
				FileInputStream fis=new FileInputStream(f);
				byte[] data=new byte[(int) f.length()];
				fis.read(data);
				streamMessage.writeString(imageName);
				streamMessage.writeInt(data.length);
				streamMessage.writeBytes(data);
				streamMessage.setStringProperty("code", textFieldTo.getText());
				messageProducer.send(streamMessage);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		comboBoxImage.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				File f=new File("images/"+newValue);
				Image image=new Image(f.toURI().toString());
				imageView.setImage(image);
			}
		});
		buttonConnecter.setOnAction(new EventHandler<ActionEvent>() {
			
			public void handle(ActionEvent event) {
				try {
					hbox.setDisable(true);
					String codeUser=textField1.getText();
					String host=textField2.getText();
					int port=Integer.parseInt(textField3.getText());
					
					connectionFactory=new ActiveMQConnectionFactory("tcp://"+host+":"+port);
				    connection = connectionFactory.createConnection();
					connection.start();
					session=connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					Destination destination = session.createTopic("enset Chat");
					messageConsumer=session.createConsumer(destination,"code='"+codeUser+"'");
					messageProducer=session.createProducer(destination);
					messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
					messageConsumer.setMessageListener(message->{
				
					    	try {
					    		if(message instanceof TextMessage) {
								TextMessage textMessage=(TextMessage) message;
								observableListMessages.add(textMessage.getText());
					    		}
					    		else if(message instanceof StreamMessage) {
					    			StreamMessage streamMessage= (StreamMessage) message;
					    			String imageName=streamMessage.readString();
					    			int size=streamMessage.readInt();
					    			byte[] data=new byte[size];
					    			streamMessage.readBytes(data);
					    			ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(data);
					    			Image image=new Image(byteArrayInputStream);
					    			imageView.setImage(image);
					    			observableListMessages.add("Reception de l'image "+imageName);
						    		}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					    
					});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}

}
