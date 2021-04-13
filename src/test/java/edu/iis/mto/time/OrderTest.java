package edu.iis.mto.time;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTest {

    private static final OrderItem DUMMY_ORDER_ITEM = new OrderItem();

    @Mock
    private Clock clockMock;
    private Order order;
    private Instant startTime = Instant.parse("2000-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        when(clockMock.getZone()).thenReturn(ZoneId.systemDefault());
        order = new Order(clockMock);
    }

    @Test
    void orderSubmittedWithTheSameDateShouldHasConfirmedState() {

        Instant expirationTime = startTime.plus(0, ChronoUnit.HOURS);
        when(clockMock.instant()).thenReturn(startTime).thenReturn(expirationTime);

        order.addItem(DUMMY_ORDER_ITEM);
        order.submit();
        order.confirm();

        assertSame(Order.State.CONFIRMED, order.getOrderState());
    }

    @Test
    void orderTimeExpiredShouldThrowAnException() {

        Instant expirationTime = startTime.plus(25, ChronoUnit.HOURS);
        when(clockMock.instant()).thenReturn(startTime).thenReturn(expirationTime);

        order.addItem(DUMMY_ORDER_ITEM);
        order.submit();

        assertThrows(OrderExpiredException.class, () -> order.confirm());
        assertSame(order.getOrderState(), Order.State.CANCELLED);
    }

    @Test
    void confirmationOfAnOrderOneSecondBeforeExpiration() {

        Instant expirationTime = startTime.plus(23, ChronoUnit.HOURS)
                .plus(59, ChronoUnit.MINUTES)
                .plus(59, ChronoUnit.SECONDS);
        when(clockMock.instant()).thenReturn(startTime).thenReturn(expirationTime);

        order.addItem(DUMMY_ORDER_ITEM);
        order.submit();
        order.confirm();

        assertSame(Order.State.CONFIRMED, order.getOrderState());
    }


}
