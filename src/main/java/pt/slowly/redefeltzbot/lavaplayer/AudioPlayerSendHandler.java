package pt.slowly.redefeltzbot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.dv8tion.jda.api.audio.AudioSendHandler;

public class AudioPlayerSendHandler implements AudioSendHandler {
    
    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;
    
    public AudioPlayerSendHandler(AudioPlayer audioPlayer){
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(1824);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        return this.audioPlayer.provide(this.frame);
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return (ByteBuffer) this.buffer.flip();
    }

    @Override
    public boolean isOpus() {
        return true;
    }
    
    
    
}
