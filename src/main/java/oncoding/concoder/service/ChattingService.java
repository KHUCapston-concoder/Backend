package oncoding.concoder.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import oncoding.concoder.dto.ChatDTO.DummyResponse;
import oncoding.concoder.dto.ChatDTO.ExitResponse;
import oncoding.concoder.dto.ChatDTO.MessageRequest;
import oncoding.concoder.dto.ChatDTO.MessageResponse;
import oncoding.concoder.dto.ChatDTO.SessionRequest;
import oncoding.concoder.dto.ChatDTO.SessionResponse;
import oncoding.concoder.model.Room;
import oncoding.concoder.model.Session;
import oncoding.concoder.model.User;
import oncoding.concoder.repository.RoomRepository;
import oncoding.concoder.repository.SessionRepository;
import oncoding.concoder.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class ChattingService {

  private final UserRepository userRepository;
  private final RoomRepository roomRepository;
  private final SessionRepository sessionRepository;


  /**
   * 
   * @param request
   * @return request 내용을 바탕으로 생성된 메시지의 responseDTO 리턴
   */
  public MessageResponse sendMessage(final MessageRequest request) {
    User user = userRepository.findById(request.getUserId()).orElseThrow(IllegalArgumentException::new);
    return new MessageResponse(user.getId(), request.getContent()); //보낼 메세지 객체를 리턴
  }

  /**
   * 
   * @param roomId
   * @param request
   * @return sessionResponse - 결론적으로 roomId에 해당되는 users임
   */

  public SessionResponse enter(final UUID roomId, final SessionRequest request) {
    User user = userRepository.findById(request.getUserId()).orElseThrow(IllegalArgumentException::new);
    Room room = roomRepository.findById(roomId).orElseThrow(IllegalArgumentException::new);
    Session session = new Session();
    session = sessionRepository.save(session);//찐 id

    sessionRepository.save(new Session(session.getId(),request.getSessionId(), user, room));

    return SessionResponse.from(room.users()); //sessionResponse 생성 - room의 users를 가지고 있음
  }

  /**
   *
   * @param sessionId
   * @return 해당 sessionId가 있었던, 즉 나가는 방에서 해당 세션 삭제 후의 유저들을 반환
   */
  public ExitResponse exit(final String sessionId) {
    Session session = sessionRepository.findBySessionId(sessionId).orElseThrow(IllegalArgumentException::new);
    Room room = session.getRoom();//해당 session을 가지고 있는 room 찾음

    session.delete(); //해당 session을 가지고 있는 user에서 session을 null로 변경, room이 가지고 있는 session 리스트에서 해당 session을 없앰
    sessionRepository.delete(session); //session자체를 삭제

    return new ExitResponse(room.getId(), SessionResponse.from(room.users()));
  }


  public DummyResponse createDummy() {
    List<User> users = new ArrayList<>();
    List<Room> rooms = new ArrayList<>();

    users.add(userRepository.save(new User("와일더")));
    users.add(userRepository.save(new User("마이클")));
    users.add(userRepository.save(new User("제이슨")));
    users.add(userRepository.save(new User("오스카")));

    rooms.add(roomRepository.save(new Room(2)));
    rooms.add(roomRepository.save(new Room(4)));
    rooms.add(roomRepository.save(new Room(5)));

    return DummyResponse.of(users, rooms);
  }

}
