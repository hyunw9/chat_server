package org.example.Model.Message;

import java.nio.channels.SocketChannel;

public class MessageTask {

  private String type;
  private String data;
  private String name;
  private String roomId;
  private String text;
  private String title;
  private SocketChannel clientSocket;

  public void setClientSocket(SocketChannel clientSocket) {
    this.clientSocket = clientSocket;
  }

  public MessageTask() {
  }

  public SocketChannel getClientSocket() {
    return clientSocket;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
