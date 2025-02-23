package instagram_clone.sgdevcamp_jikji_insta_clone_notification_server.sse;

//import static instagram_clone.sgdevcamp_jikji_insta_clone_notification_server.sse.SseController.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import instagram_clone.sgdevcamp_jikji_insta_clone_notification_server.sse.domain.Notification;
import instagram_clone.sgdevcamp_jikji_insta_clone_notification_server.sse.dto.NotificationResponse;
import instagram_clone.sgdevcamp_jikji_insta_clone_notification_server.sse.dto.NotificationSseDto;
import instagram_clone.sgdevcamp_jikji_insta_clone_notification_server.sse.dto.SliceResponseDto;
import instagram_clone.sgdevcamp_jikji_insta_clone_notification_server.sse.repository.EmitterRepository;
import instagram_clone.sgdevcamp_jikji_insta_clone_notification_server.sse.repository.NotificationRepository;

@Service
public class NotificationService {
	private static final Long DEFAULT_SSE_TIMEOUT = 1000 * 60 * 60 * 12L;
	EmitterRepository emitterRepository;
	NotificationRepository notificationRepository;

	public NotificationService(EmitterRepository emitterRepository, NotificationRepository notificationRepository) {
		this.emitterRepository = emitterRepository;
		this.notificationRepository = notificationRepository;
	}

	public SseEmitter subscribe(String pk, String lastEventId) {
		String id = pk + "_" + System.currentTimeMillis();
		SseEmitter emitter = emitterRepository.save(id, new SseEmitter(DEFAULT_SSE_TIMEOUT));

		emitter.onCompletion(() -> emitterRepository.deleteById(id));
		emitter.onTimeout(() -> emitterRepository.deleteById(id));
		emitter.onError((e) -> emitterRepository.deleteById(id));

		sendToClient(emitter, id, "Server-Sent-Event Created. [userId" + pk + "]");

		if (!lastEventId.isEmpty()) {
			Map<String, SseEmitter> events = emitterRepository.findAllWithId(pk);
			events.entrySet().stream()
				.filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
				.forEach(entry -> sendToClient(emitter, entry.getKey(), entry.getValue()));
		}

		return emitter;
	}

	private void sendToClient(SseEmitter emitter, String id, Object data) {
		try {
			emitter.send(SseEmitter.event()
				.id(id)
				.name("sse")
				.data(data));
		} catch (IOException e) {
			emitterRepository.deleteById(id);
			throw new RuntimeException("Connection Error");
		}
	}

	public void send(String senderNickname, String receiverId, String type) {
		String content = "";
		if (Objects.equals(type, "chat")) {
			content = senderNickname + "님께서 메시지를 보내셨습니다";
		} else if (Objects.equals(type, "post")) {
			content = senderNickname + "님께서 포스트를 올렸습니다";
		} else if (Objects.equals(type, "follow")) {
			content = senderNickname + "님께서 팔로우를 요청하셨습니다";
			System.out.println("content = " + content);
			System.out.println("senderNickname = " + senderNickname);
			System.out.println("receiverId = " + receiverId);
		}
		Map<String, SseEmitter> sseEmitters = emitterRepository.findAllWithId(receiverId);
		NotificationSseDto notificationSseDto = NotificationSseDto.builder().content(content).build();
		sseEmitters.forEach(
			(key, emitter) -> {
				emitterRepository.saveEventCache(receiverId, emitter);
				sendToClient(emitter, key, notificationSseDto);
			}
		);
	}

	public Notification findById(Long id) {
		Notification notification = notificationRepository.findById(id).get();
		return notification;
	}

	public List<Notification> findByUserId(Long userId) {
		Collection<Notification> notifications = notificationRepository.findByUserId(userId);
		return new ArrayList<>(notifications);
	}

	public Long save(Notification notification) {
		notificationRepository.save(notification);
		return notification.getId();
	}

	public void delete(Notification notification) {
		notificationRepository.delete(notification);
	}

	//알림 읽음처리 부분
	//더 나은 방법이 있을 것 같음. 질문하기
	public void readNotification(Long id) {
		Notification notification = notificationRepository.findById(id).get();
		notification.readNotification();
		notificationRepository.save(notification);
	}

	public SliceResponseDto findAllNotifications(Long lastStudyId, Integer size) {
		Slice<Notification> notification = notificationRepository.findAllOrderByNotificationIdDesc(lastStudyId,
			Pageable.ofSize(size));
		return SliceResponseDto.create(notification, NotificationResponse::from);
	}

}
