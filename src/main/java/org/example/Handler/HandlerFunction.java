package org.example.Handler;

import java.util.List;
import java.util.NoSuchElementException;
import org.example.Model.ChatRoomManager;
import org.example.Model.Res.JsonMessage;
import org.example.Model.Res.SCChatRes;
import org.example.Model.Res.SCRoom;
import org.example.Model.Res.SCRoomListRes;
import org.example.Model.Res.SCSystemMessageRes;
import org.example.Model.Message.MessageTask;
import org.example.Model.Room;
import org.example.Model.User;
import org.example.Model.UserManager;

public class HandlerFunction {

  private static ChatRoomManager chatRoomManager;
  private static UserManager userManager;

  public HandlerFunction(ChatRoomManager chatRoomManager, UserManager userManager) {
    HandlerFunction.chatRoomManager = chatRoomManager;
    HandlerFunction.userManager = userManager;
  }

  //채팅 이름 지정 요청 처리
  public static void on_cs_name(MessageTask task) {
    boolean userIn = chatRoomManager.checkUserInRoom(task.getClientSocket());

    if (userIn) {
      User user = chatRoomManager.findUserInfo(task.getClientSocket());
      System.out.println(user.getName());
      user.setName(task.getName());

    } else {//유저가 방에 없으면
      userManager.setUserName(task.getClientSocket(), task.getName());
    }
    String msg = "이름이" + task.getName() + " 으로 변경되었습니다.";
    JsonMessage StoC = new SCSystemMessageRes(msg);

    if (userIn) {
      Room room = chatRoomManager.findRoomByUserSocket(task.getClientSocket());
      chatRoomManager.broadcastMsgToRoom(room.getId(), StoC);
    } else {
      userManager.sendMessage(task.getClientSocket(), StoC);
    }
  }

  //채팅 방 목록 요청 처리
  //TODO Create,Join 만든 후 구현할것
  public static void on_cs_rooms(MessageTask task) {
    List<SCRoom> roomList = chatRoomManager.getRoomInfoList();
    JsonMessage SCRoomListRes = new SCRoomListRes(roomList);
    userManager.sendMessage(task.getClientSocket(),SCRoomListRes);
  }

  //채팅 방 참여 요청 처리
  public static void on_cs_join(MessageTask task) {
    int roomId = Integer.parseInt(task.getRoomId());
    boolean userIn = chatRoomManager.checkUserInRoom(task.getClientSocket());
    boolean userIsNotInRoom = chatRoomManager.isRoom(roomId);

    if (userIn) { //이미 유저가 방에 속한 경우
      JsonMessage SCSystemMessage = new SCSystemMessageRes("이미 방에 속한 유저입니다! ");
      chatRoomManager.sendMessage(task.getClientSocket(), SCSystemMessage);
    } else if (!userIsNotInRoom) { //유저가 방에 속해있지 않은 경우
      JsonMessage SCSystemMessage = new SCSystemMessageRes("존재하지 않는 방입니다.");
      userManager.sendMessage(task.getClientSocket(), SCSystemMessage);
    } else {
      User user = userManager.findUser(task.getClientSocket())
          .orElseThrow(NoSuchElementException::new);

      userManager.removeUser(user.getSocketChannel());
      Room room = chatRoomManager.findRoomByRoomId(roomId).orElseThrow(NoSuchElementException::new);
      chatRoomManager.addUserToChatRoom(room, user);
      JsonMessage SCSystemMessage = new SCSystemMessageRes(user.getName() + " 님이 채팅방에 입장하셨습니다. ");
      chatRoomManager.broadcastMsgToRoom(room.getId(), SCSystemMessage);
    }
  }


  //채팅 방 만들기 요청 처리
  public static void on_cs_create(MessageTask task) {
    boolean userIn = chatRoomManager.checkUserInRoom(task.getClientSocket());

    if (userIn) {//방 내부에서 방 생성할경우
      JsonMessage SCSystemMessageRes = new SCSystemMessageRes("이미 방에 속한 유저입니다!");
      chatRoomManager.sendMessage(task.getClientSocket(), SCSystemMessageRes);
    } else {
      //방에 속해있지 않으면
      User user = userManager.findUser(task.getClientSocket())
          .orElseThrow(NoSuchElementException::new);
      userManager.removeUser(user.getSocketChannel());

      Room newRoom = new Room(task.getTitle());
      chatRoomManager.addUserToChatRoom(newRoom, user);

      System.out.println("id: " + newRoom.getId() + " title: " + newRoom.getTitle() + " 방 생성");
      JsonMessage SCSystemMessageRes = new SCSystemMessageRes(task.getTitle() + " 방을 생성했습니다.");
      chatRoomManager.sendMessage(user.getSocketChannel(), SCSystemMessageRes);
    }
  }

  //채팅 방 나가기 요청 처리
  public static void on_cs_leave(MessageTask task) {
    boolean userIn = chatRoomManager.checkUserInRoom(task.getClientSocket());

    if (!userIn) {//방이 없다면
      JsonMessage SCSystemMessageRes = new SCSystemMessageRes("방에 속해있지 않습니다.");
      userManager.sendMessage(task.getClientSocket(), SCSystemMessageRes);
    } else {//방에 속해있다면
      Room userRoom = chatRoomManager.findRoomByUserSocket(task.getClientSocket());
      User user = chatRoomManager.findUserInfo(task.getClientSocket());
      chatRoomManager.deleteUser(userRoom, user.getSocketChannel());
      userManager.addUser(user);
      JsonMessage SCSystemMessageRes = new SCSystemMessageRes(
          userRoom.getTitle() + " 방에서 나갔습니다. ");
      chatRoomManager.broadcastMsgToRoom(userRoom.getId(), SCSystemMessageRes);
      userManager.sendMessage(user.getSocketChannel(), SCSystemMessageRes);
    }
  }

  //채팅 메세지 전송 요청 처리
  //TODO 채팅방 생성 , 조인 이후 테스트할것
  public static void on_cs_chat(MessageTask task) {
    boolean userIn = chatRoomManager.checkUserInRoom(task.getClientSocket());

    if (!userIn) {
      String msg = "현재 대화방에 들어가 있지 않습니다.";
      JsonMessage scSystemMessage = new SCSystemMessageRes(msg);
      chatRoomManager.sendMessage(task.getClientSocket(), scSystemMessage);
    } else {
      Room room = chatRoomManager.findRoomByUserSocket(task.getClientSocket());
      String msg = task.getText();
      User user = chatRoomManager.findUserInfo(task.getClientSocket());
      JsonMessage scChatRes = new SCChatRes(msg, user.getName());
      chatRoomManager.broadcastMsgToRoom(room.getId(), scChatRes);
    }
  }

  //채팅 서버 종료 요청 처리
  public static void on_cs_shutdown(String s) {
    System.out.println("/shutdown ");
  }
}
