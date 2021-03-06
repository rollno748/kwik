/*
 * Copyright © 2020 Peter Doornbosch
 *
 * This file is part of Kwik, a QUIC client Java library
 *
 * Kwik is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Kwik is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.luminis.quic.packet;

import net.luminis.quic.InvalidPacketException;
import net.luminis.quic.Version;
import net.luminis.quic.log.Logger;
import net.luminis.tls.ByteUtils;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class RetryPacketTest {

    public static final int DONT_CARE = -1;
    public static final String QUIC_VERSION_AS_HEX = Integer.toHexString(Version.getDefault().getId());

    @Test
    void parseRetryPacket() throws Exception {
        String data = ("0f " + QUIC_VERSION_AS_HEX + "040d0d0d0d 040e0e0e0e 0102030405060708090a0b0c0d0e0f10").replace(" ", "");
        ByteBuffer buffer = ByteBuffer.wrap(ByteUtils.hexToBytes(data));

        RetryPacket retry = new RetryPacket(Version.getDefault());
        retry.parse(buffer, null, DONT_CARE, mock(Logger.class), DONT_CARE);

        assertThat(retry.getRetryToken()).hasSize(0);
        assertThat(retry.validateIntegrityTag(new byte[] { 0x0e, 0x0e, 0x0e, 0x0e })).isFalse();
    }

    @Test
    void parseEmtpyRetryPacket() throws Exception {
        ByteBuffer data = ByteBuffer.wrap(new byte[] { (byte) 0xf0 });

        assertThatThrownBy(() ->
                new RetryPacket(Version.getDefault()).parse(data, null, DONT_CARE, mock(Logger.class), DONT_CARE)
        ).isInstanceOf(InvalidPacketException.class);
    }

    @Test
    void parseRetryPacketWithIncompleteHeader() throws Exception {
        ByteBuffer data = ByteBuffer.wrap(new byte[] { (byte) 0xf0, 0x00, 0x00, 0x00, 0x01 });

        assertThatThrownBy(() ->
                new RetryPacket(Version.getDefault()).parse(data, null, DONT_CARE, mock(Logger.class), DONT_CARE)
        ).isInstanceOf(InvalidPacketException.class);
    }

    @Test
    void packetWithOtherVersionShouldBeIgnored() throws Exception {
        ByteBuffer data = ByteBuffer.wrap(new byte[] { (byte) 0xf0, 0x00, 0x00, 0x00, 0x0f, 0x04, 0x01, 0x02, 0x03, 0x04, 0x04, 0x01, 0x02, 0x03, 0x04 });

        assertThatThrownBy(() ->
                new RetryPacket(Version.getDefault()).parse(data, null, DONT_CARE, mock(Logger.class), DONT_CARE)
        ).isInstanceOf(InvalidPacketException.class);
    }

    @Test
    void parseRetryPacketWithInvalidSourceConnectionIdLength() throws Exception {
        String data = ("0f " + QUIC_VERSION_AS_HEX + "3f0d0d0d0d 040e0e0e0e").replace(" ", "");
        ByteBuffer buffer = ByteBuffer.wrap(ByteUtils.hexToBytes(data));

        assertThatThrownBy(() ->
                new RetryPacket(Version.getDefault()).parse(buffer, null, DONT_CARE, mock(Logger.class), DONT_CARE)
        ).isInstanceOf(InvalidPacketException.class);
    }

    @Test
    void parseRetryPacketWithInvalidSourceConnectionIdLength2() throws Exception {
        String data = ("0f " + QUIC_VERSION_AS_HEX + "180d0d0d0d 040e0e0e0e 0102030405060708090a0b0c0d0e0f").replace(" ", "");
        ByteBuffer buffer = ByteBuffer.wrap(ByteUtils.hexToBytes(data));

        assertThatThrownBy(() ->
                new RetryPacket(Version.getDefault()).parse(buffer, null, DONT_CARE, mock(Logger.class), DONT_CARE)
        ).isInstanceOf(InvalidPacketException.class);
    }

    @Test
    void parseRetryPacketWithInvalidDestinationConnectionIdLength() throws Exception {
        String data = ("0f " + QUIC_VERSION_AS_HEX + "040d0d0d0d 400e0e0e0e").replace(" ", "");
        ByteBuffer buffer = ByteBuffer.wrap(ByteUtils.hexToBytes(data));

        assertThatThrownBy(() ->
                new RetryPacket(Version.getDefault()).parse(buffer, null, DONT_CARE, mock(Logger.class), DONT_CARE)
        ).isInstanceOf(InvalidPacketException.class);
    }

    @Test
    void parseRetryPacketWithIncompleteRetryIntegrityTag() throws Exception {
        String data = ("0f " + QUIC_VERSION_AS_HEX + "040d0d0d0d 040e0e0e0e 0102030405060708090a0b0c0d0e").replace(" ", "");
        ByteBuffer buffer = ByteBuffer.wrap(ByteUtils.hexToBytes(data));

        assertThatThrownBy(() ->
                new RetryPacket(Version.getDefault()).parse(buffer, null, DONT_CARE, mock(Logger.class), DONT_CARE)
        ).isInstanceOf(InvalidPacketException.class);
    }

}
