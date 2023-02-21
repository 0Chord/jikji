package instagram_clone.sgdevcamp_jikji_insta_clone_notification_server.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
	private Integer senderId;
	private Integer receiverId;
	private String type;
}
