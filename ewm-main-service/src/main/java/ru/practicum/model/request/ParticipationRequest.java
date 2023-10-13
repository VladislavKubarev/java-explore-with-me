package ru.practicum.model.request;

import lombok.*;
import ru.practicum.model.event.Event;
import ru.practicum.model.request.enums.RequestStatus;
import ru.practicum.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "requests", schema = "public")
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequest {
    @Id
    @Column(name = "request_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User requester;
    private LocalDateTime created;
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
