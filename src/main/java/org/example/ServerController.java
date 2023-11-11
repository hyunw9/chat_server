package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.example.Handler.HandlerFunction;
import org.example.Handler.HandlerMap;
import org.example.View.Input;
import org.example.View.Output;

public class ServerController {

  private static final Input input = new Input();
  private static final Output output = new Output();
  private static ThreadPool threadPool;
  private static HandlerMap handlerMap;
  private static Set<SocketChannel> allClients;

  public static void startChatServer() throws IOException, InterruptedException {

    output.askThreadCount();
    allClients = new HashSet<>();
    handlerMap = HandlerMap.addInitialFuncAndCreateMap();
    threadPool = new ThreadPool(input.getInput(), handlerMap);
    for (String s : handlerMap.getHandlerMap().keySet()) {
      System.out.println(s);
    }

    while (true) {
      Selector selector = Selector.open();

      ServerSocketChannel server = ServerSocketChannel.open();
      InetSocketAddress socketAddress = new InetSocketAddress(9142);
      server.bind(socketAddress);

      output.printServerOpenedPort(socketAddress);

      server.configureBlocking(false);

      server.register(selector, SelectionKey.OP_ACCEPT);

      while (true) {
        selector.select();

        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
          SelectionKey key = keyIterator.next();
          keyIterator.remove();

          if (!key.isValid()) {
            continue;
          }

          if (key.isAcceptable()) {
            acceptSocket(selector, key);
          } else if (key.isReadable()) {
            readSocket(key);
          }
        }
      }
    }
  }

  private static void acceptSocket(Selector selector, SelectionKey key) throws IOException {
    ServerSocketChannel serverSock = (ServerSocketChannel) key.channel();
    SocketChannel client = serverSock.accept();
    allClients.add(client);
    client.configureBlocking(false);
    client.register(selector, SelectionKey.OP_READ);
    System.out.format("Accepted: %s%n", client.socket().getRemoteSocketAddress().toString());
  }

  public static void readSocket(SelectionKey key) throws IOException, InterruptedException {
    ByteBuffer buf = ByteBuffer.allocate(80);

    SocketChannel socketChannel = (SocketChannel) key.channel();
    buf.clear();
    int byteRead = socketChannel.read(buf);

    if (byteRead == -1) {
      throw new InterruptedException();
    } else {
      buf.flip();
      String msg = StandardCharsets.UTF_8.decode(buf).toString();
      System.out.format("Message Received(%d): %s%n", byteRead, msg);

      if (msg.equals("Done")) {
        throw new InterruptedException();
      }
    }
  }
}

