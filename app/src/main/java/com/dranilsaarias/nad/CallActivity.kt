package com.dranilsaarias.nad

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.ImageView
import android.widget.Toast
import com.dranilsaarias.nad.util.CameraCapturerCompat
import com.twilio.video.*
import kotlinx.android.synthetic.main.content_call.*


class CallActivity : AppCompatActivity() {
    lateinit var r: Ringtone

    private var primaryVideoView: VideoView? = null
    private var thumbnailVideoView: VideoView? = null
    private var muteActionFab: ImageView? = null
    private var connectActionFab: ImageView? = null

    private var audioManager: AudioManager? = null
    private var participantIdentity: String? = null

    private var localAudioTrack: LocalAudioTrack? = null
    private var localVideoTrack: LocalVideoTrack? = null

    private var previousAudioMode: Int = 0
    private var previousMicrophoneMute: Boolean = false
    private var localVideoView: VideoRenderer? = null
    private var disconnectedFromOnDestroy: Boolean = false
    private var cameraCapturerCompat: CameraCapturerCompat? = null

    private var room: Room? = null
    private var localParticipant: LocalParticipant? = null

    private var accessToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        val window = this.window
        window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON)
        window.setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN)

        primaryVideoView = findViewById(R.id.primary_video_view)
        thumbnailVideoView = findViewById(R.id.thumbnail_video_view)
        connectActionFab = findViewById(R.id.connect_action_fab)
        muteActionFab = findViewById(R.id.mute_action_fab)

        volumeControlStream = AudioManager.STREAM_VOICE_CALL
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        intializeUI()

        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        r = RingtoneManager.getRingtone(applicationContext, notification)
        r.play()

        answer.setOnClickListener {
            incoming_view.visibility = View.GONE
            call_view.visibility = View.VISIBLE
            r.stop()
            if (!checkPermissionForCameraAndMicrophone()) {
                requestPermissionForCameraAndMicrophone()
            } else {
                createAudioAndVideoTracks()
                //setAccessToken()
            }

            if (intent.hasExtra("token")) {
                accessToken = intent.getStringExtra("token")
                val room = intent.getStringExtra("room")
                connectToRoom(room)
            }
        }

        decline.setOnClickListener {
            r.stop()
            finish()
        }
    }

    private fun intializeUI() {
        connectActionFab!!.setImageResource(R.drawable.llamar_terminar)
        connectActionFab!!.setOnClickListener(connectActionClickListener())

        muteActionFab!!.setOnClickListener(muteClickListener())
    }

    private fun createAudioAndVideoTracks() {
        // Share your microphone
        localAudioTrack = LocalAudioTrack.create(this, true)

        // Share your camera
        cameraCapturerCompat = CameraCapturerCompat(this, CameraCapturer.CameraSource.FRONT_CAMERA)
        localVideoTrack = LocalVideoTrack.create(this, true, cameraCapturerCompat!!.videoCapturer)
        primaryVideoView!!.mirror = true
        localVideoTrack!!.addRenderer(primaryVideoView)
        localVideoView = primaryVideoView
    }

    private fun connectActionClickListener(): View.OnClickListener {
        return View.OnClickListener {
            if (room != null) {
                room!!.disconnect()
            }
            finish()
        }
    }

    private fun muteClickListener(): View.OnClickListener {
        return View.OnClickListener {
            /*
         * Enable/disable the local audio track. The results of this operation are
         * signaled to other Participants in the same Room. When an audio track is
         * disabled, the audio is muted.
         */
            if (localAudioTrack != null) {
                val enable = !localAudioTrack!!.isEnabled
                localAudioTrack!!.enable(enable)
                val icon = if (enable)
                    R.drawable.llamar_volumen
                else
                    R.drawable.llamar_mute
                muteActionFab!!.setImageResource(icon)
            }
        }
    }

    private fun checkPermissionForCameraAndMicrophone(): Boolean {
        val resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        return resultCamera == PackageManager.PERMISSION_GRANTED && resultMic == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionForCameraAndMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this,
                    R.string.permissions_needed,
                    Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                    CAMERA_MIC_PERMISSION_REQUEST_CODE)
        }
    }

    private fun connectToRoom(roomName: String) {
        configureAudio(true)
        val connectOptionsBuilder = ConnectOptions.Builder(accessToken)
                .roomName(roomName)

        /*
         * Add local audio track to connect options to share with participants.
         */
        if (localAudioTrack != null) {
            connectOptionsBuilder
                    .audioTracks(listOf<LocalAudioTrack>(localAudioTrack!!))
        }

        /*
         * Add local video track to connect options to share with participants.
         */
        if (localVideoTrack != null) {
            connectOptionsBuilder.videoTracks(listOf<LocalVideoTrack>(localVideoTrack!!))
        }
        room = Video.connect(this, connectOptionsBuilder.build(), roomListener())
    }

    private fun addParticipant(participant: Participant) {
        /*
         * This app only displays video for one additional participant per Room
         */
        if (thumbnailVideoView!!.visibility == View.VISIBLE) {
            Snackbar.make(connectActionFab!!,
                    "Multiple participants are not currently support in this UI",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            return
        }
        participantIdentity = participant.identity

        /*
         * Add participant renderer
         */
        if (participant.videoTracks.size > 0) {
            addParticipantVideo(participant.videoTracks[0])
        }

        /*
         * Start listening for participant events
         */
        participant.setListener(participantListener())
    }

    /*
     * Set primary view as renderer for participant video track
     */
    private fun addParticipantVideo(videoTrack: VideoTrack) {
        moveLocalVideoToThumbnailView()
        primaryVideoView!!.mirror = false
        videoTrack.addRenderer(primaryVideoView)
    }

    private fun moveLocalVideoToThumbnailView() {
        if (thumbnailVideoView!!.visibility == View.GONE) {
            thumbnailVideoView!!.visibility = View.VISIBLE
            localVideoTrack!!.removeRenderer(primaryVideoView)
            localVideoTrack!!.addRenderer(thumbnailVideoView)
            localVideoView = thumbnailVideoView
            thumbnailVideoView!!.mirror = cameraCapturerCompat!!.cameraSource == CameraCapturer.CameraSource.FRONT_CAMERA
        }
    }

    /*
     * Called when participant leaves the room
     */
    private fun removeParticipant(participant: Participant) {
        if (participant.identity != participantIdentity) {
            return
        }

        /*
         * Remove participant renderer
         */
        if (participant.videoTracks.size > 0) {
            removeParticipantVideo(participant.videoTracks[0])
        }
        moveLocalVideoToPrimaryView()
    }

    private fun removeParticipantVideo(videoTrack: VideoTrack) {
        videoTrack.removeRenderer(primaryVideoView)
    }

    private fun moveLocalVideoToPrimaryView() {
        if (thumbnailVideoView!!.visibility == View.VISIBLE) {
            localVideoTrack!!.removeRenderer(thumbnailVideoView)
            thumbnailVideoView!!.visibility = View.GONE
            localVideoTrack!!.addRenderer(primaryVideoView)
            localVideoView = primaryVideoView
            primaryVideoView!!.mirror = cameraCapturerCompat!!.cameraSource == CameraCapturer.CameraSource.FRONT_CAMERA
        }
    }

    private fun roomListener(): Room.Listener {
        return object : Room.Listener {
            override fun onConnected(room: Room) {
                localParticipant = room.localParticipant

                for (participant in room.participants) {
                    addParticipant(participant)
                    break
                }
            }

            override fun onConnectFailure(room: Room, e: TwilioException?) {
                Log.e("error", e.toString())
                configureAudio(false)
            }

            override fun onDisconnected(room: Room, e: TwilioException?) {
                localParticipant = null
                this@CallActivity.room = null
                // Only reinitialize the UI if disconnect was not called from onDestroy()
                if (!disconnectedFromOnDestroy) {
                    configureAudio(false)
                    //intializeUI()
                    //moveLocalVideoToPrimaryView()
                }
            }

            override fun onParticipantConnected(room: Room, participant: Participant) {
                addParticipant(participant)

            }

            override fun onParticipantDisconnected(room: Room, participant: Participant) {
                removeParticipant(participant)
                finish()
            }

            override fun onRecordingStarted(room: Room) {
                /*
                 * Indicates when media shared to a Room is being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStarted")
            }

            override fun onRecordingStopped(room: Room) {
                /*
                 * Indicates when media shared to a Room is no longer being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStopped")
            }
        }
    }

    private fun participantListener(): Participant.Listener {
        return object : Participant.Listener {
            override fun onAudioTrackAdded(participant: Participant, audioTrack: AudioTrack) {
            }

            override fun onAudioTrackRemoved(participant: Participant, audioTrack: AudioTrack) {
            }

            override fun onVideoTrackAdded(participant: Participant, videoTrack: VideoTrack) {
                addParticipantVideo(videoTrack)
            }

            override fun onVideoTrackRemoved(participant: Participant, videoTrack: VideoTrack) {
                removeParticipantVideo(videoTrack)
            }

            override fun onAudioTrackEnabled(participant: Participant, audioTrack: AudioTrack) {

            }

            override fun onAudioTrackDisabled(participant: Participant, audioTrack: AudioTrack) {

            }

            override fun onVideoTrackEnabled(participant: Participant, videoTrack: VideoTrack) {

            }

            override fun onVideoTrackDisabled(participant: Participant, videoTrack: VideoTrack) {

            }
        }
    }

    private fun configureAudio(enable: Boolean) {
        if (enable) {
            previousAudioMode = audioManager!!.mode
            // Request audio focus before making any device switch.
            audioManager!!.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            /*
             * Use MODE_IN_COMMUNICATION as the default audio mode. It is required
             * to be in this mode when playout and/or recording starts for the best
             * possible VoIP performance. Some devices have difficulties with
             * speaker mode if this is not set.
             */
            audioManager!!.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager!!.isSpeakerphoneOn = true
            /*
             * Always disable microphone mute during a WebRTC call.
             */
            previousMicrophoneMute = audioManager!!.isMicrophoneMute
            audioManager!!.isMicrophoneMute = false
        } else {
            audioManager!!.mode = previousAudioMode
            audioManager!!.abandonAudioFocus(null)
            audioManager!!.isMicrophoneMute = previousMicrophoneMute
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {
            var cameraAndMicPermissionGranted = true

            for (grantResult in grantResults) {
                cameraAndMicPermissionGranted = cameraAndMicPermissionGranted and (grantResult == PackageManager.PERMISSION_GRANTED)
            }

            if (cameraAndMicPermissionGranted) {
                createAudioAndVideoTracks()
                //setAccessToken()
            } else {
                Toast.makeText(this,
                        R.string.permissions_needed,
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        if (room != null && room!!.state != RoomState.DISCONNECTED) {
            room!!.disconnect()
            disconnectedFromOnDestroy = true
        }

        /*
         * Release the local audio and video tracks ensuring any memory allocated to audio
         * or video is freed.
         */
        if (localAudioTrack != null) {
            localAudioTrack!!.release()
            localAudioTrack = null
        }
        if (localVideoTrack != null) {
            localVideoTrack!!.release()
            localVideoTrack = null
        }
        super.onDestroy()
        if (r.isPlaying) {
            r.stop()
        }
        TwilioService.startService(this)
    }

    override fun onResume() {
        super.onResume()
        /*
         * If the local video track was released when the app was put in the background, recreate.
         */
        if (localVideoTrack == null && checkPermissionForCameraAndMicrophone() && call_view.visibility == View.VISIBLE) {
            localVideoTrack = LocalVideoTrack.create(this, true, cameraCapturerCompat!!.videoCapturer)
            localVideoTrack!!.addRenderer(localVideoView)

            /*
             * If connected to a Room then share the local video track.
             */
            if (localParticipant != null) {
                localParticipant!!.addVideoTrack(localVideoTrack!!)
            }
        }
    }

    override fun onPause() {
        /*
         * Release the local video track before going in the background. This ensures that the
         * camera can be used by other applications while this app is in the background.
         */
        if (localVideoTrack != null) {
            /*
             * If this local video track is being shared in a Room, remove from local
             * participant before releasing the video track. Participants will be notified that
             * the track has been removed.
             */
            if (localParticipant != null) {
                localParticipant!!.removeVideoTrack(localVideoTrack!!)
            }

            localVideoTrack!!.release()
            localVideoTrack = null
        }
        super.onPause()
    }

    companion object {
        val CAMERA_MIC_PERMISSION_REQUEST_CODE = 1
        val TAG = "CallActivity"
    }
}
