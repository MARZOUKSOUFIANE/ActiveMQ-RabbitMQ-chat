package chat_RabbitMQ;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.concurrent.TimeoutException;

import javax.jms.StreamMessage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.AMQP.BasicProperties;

import javafx.application.Application;
import javafx.application.Platform;
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

public class AmqpChat extends Application{
	
	private final String EXCHANGE_NAME="chat";
	private Channel channelEmitter;
	private Channel channelReceiver;
	private ConnectionFactory connectionFactory;
	private Connection connectionEmitter;
	private Connection connectionReceiver;
	private ListView<String> listView;
	private Boolean isText;
	
	public static void main(String[] args) throws Exception{
		Application.launch(AmqpChat.class);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("AMQP Chat");
		BorderPane borderPane=new BorderPane();
		HBox hbox=new HBox();hbox.setPadding(new Insets(10));hbox.setSpacing(15);
		hbox.setBackground(new Background(new BackgroundFill(Color.BISQUE, CornerRadii.EMPTY, Insets.EMPTY)));
		
		Label label1=new Label("Code: ");
		TextField textField1=new TextField("C1");
		textField1.setPromptText("Code");
		
		Label label2=new Label("Host: ");
		TextField textField2=new TextField("localhost");
		textField2.setPromptText("Host");
		textField2.setEditable(false);
		
		Label label3=new Label("Port: ");
		TextField textField3=new TextField("61616");
		textField3.setPromptText("Port");
		textField3.setEditable(false);
		
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
					BasicProperties properties=new BasicProperties().builder()
							.type("message")
							.build();
					String message=textMessage.getText();
					channelEmitter.basicPublish(EXCHANGE_NAME, textFieldTo.getText(), properties, message.getBytes("UTF-8"));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		});
		
		buttonEnvoyerImage.setOnAction(e->{
			try {
				String imageName=comboBoxImage.getSelectionModel().getSelectedItem();
				File f=new File("images/"+imageName);
				FileInputStream fis=new FileInputStream(f);
				byte[] data=new byte[(int) f.length()];
				fis.read(data);
				try {
					BasicProperties properties=new BasicProperties().builder()
							.type("image")
							.contentType(imageName)
							.build();
					channelEmitter.basicPublish(EXCHANGE_NAME, textFieldTo.getText(), properties ,data);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
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
					hbox.setDisable(true);
					String codeUser=textField1.getText();
					String host=textField2.getText();
					int port=Integer.parseInt(textField3.getText());
					
					connectionFactory = new ConnectionFactory();
					
					try {
						connectionEmitter=connectionFactory.newConnection();
						connectionEmitter = connectionFactory.newConnection();
					    channelEmitter=connectionEmitter.createChannel();
					    channelEmitter.exchangeDeclare(EXCHANGE_NAME, "direct");
					    
					    connectionReceiver = connectionFactory.newConnection();
					    channelReceiver=connectionReceiver.createChannel();
					    channelReceiver.exchangeDeclare(EXCHANGE_NAME, "direct");
					    String queueName=channelReceiver.queueDeclare().getQueue();
					    channelReceiver.queueBind(queueName, EXCHANGE_NAME, textField1.getText());
					    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
					    	String message = new String(delivery.getBody(), "UTF-8");
					    	
					    	Platform.runLater(new Runnable() {
					    	    @Override
					    	    public void run() {
					    	        // Update UI here.
					    	    	if(delivery.getProperties().getType().equals("image")) {
						    		    ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(delivery.getBody());
						    			Image image=new Image(byteArrayInputStream);
						    			imageView.setImage(image);
						    			observableListMessages.add("Reception de l'image : "+ delivery.getProperties().getContentType());
								       }
								        
								        if(delivery.getProperties().getType().equals("message")) {					
								        	observableListMessages.add(message);
								       }
					    	    }
					    	});
					    }; 
					    
					    channelReceiver.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
					    
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
										
			}	    
		});
		
	}

}
