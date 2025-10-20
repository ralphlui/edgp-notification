package sg.edu.nus.iss.edgp.notification.utility;

import org.junit.jupiter.api.Test;

import sg.edu.nus.iss.edgp.notification.dto.NotificationDTO;

import static org.junit.jupiter.api.Assertions.*;

class DTOMapperTest {

    @Test
    void testToNotificationDTO() {
        String userName = "john_doe@gmail.com";
        boolean isSent = true;

        NotificationDTO dto = DTOMapper.toNotificationDTO(userName, isSent);

        assertNotNull(dto);
        assertEquals(userName, dto.getUserEmail());
        assertTrue(dto.isSent());
    }
}
