(ns syntorial.syntorial
  (:use [overtone.live]))

(defsynth syntorial-synth
  [note            60   ; midi note value
   amp             1.0  ; amplitude/volume
   osc1-waveform   0    ; 0=saw, 1=pulse
   osc1-pulsewidth 0.50 ;
   osc2-waveform   0    ; 0=saw, 1=pulse
   osc2-pulsewidth 0.50 ;
   osc2-semi       0    ; semitone offset
   mix             0.00 ; mix osc1/osc2
   cutoff          1.0  ; 0.0 - 1.0
   attack          0.150
   release         0.015
   master-volume   0.0  ; volume in dB
   gate            1    ; note-on/off
   out-bus         0    ; output bus
   ]
  (let [note1-freq  (midicps note)
        note2-freq  (midicps (+ note osc2-semi))
        osc1-saw    (lf-saw note1-freq)
        osc1-pulse  (pulse note1-freq osc1-pulsewidth)
        osc1        (select osc1-waveform [osc1-saw osc1-pulse])
        osc2-saw    (lf-saw note2-freq)
        osc2-pulse  (pulse note2-freq osc2-pulsewidth)
        osc2        (select osc2-waveform [osc2-saw osc2-pulse])
        osc         (+ (* mix osc2) (* (- 1 mix) osc1))
        osc-stereo  [osc osc]
        filt-stereo (lpf osc-stereo (lin-exp cutoff 0.0 1.0 20.0 20000.0))
        env1        (env-gen (asr attack 1.0 release) :gate gate :action FREE)
        master      (pow 10 (/ master-volume 20)) ; dB -> V
        ]
    (out out-bus (* master env1 filt-stereo))))

(comment

  ;; play a single note
  (def s (syntorial-synth :osc1-waveform 1))
  (ctl s :gate 0)

  ;; saw
  (def mpp (midi-poly-player (partial syntorial-synth :osc1-waveform 0)))
  ;; pulse-square
  (def mpp (midi-poly-player (partial syntorial-synth :osc1-waveform 1)))
  ;; pulse-medium
  (def mpp (midi-poly-player (partial syntorial-synth :osc1-waveform 1 :osc1-pulsewidth 0.25)))
  ;; pulse-thin
  (def mpp (midi-poly-player (partial syntorial-synth :osc1-waveform 1 :osc1-pulsewidth 0.10)))

  ;; saw + lpf
  (def mpp (midi-poly-player (partial syntorial-synth :cutoff 0.5)))

  ;; saw + lpf + master
  (def mpp (midi-poly-player (partial syntorial-synth :cutoff 0.4 :master-volume 6.0)))

  ;; saw + lpf + master + attack/release
  ;; On your own 3
  ;; Bass
  (def mpp (midi-poly-player (partial syntorial-synth
                                      :osc1-waveform   1
                                      :osc1-pulsewidth 0.4
                                      :cutoff          0.4
                                      :master-volume   8.0
                                      :attack          0.005
                                      :release         0.25
                                      )))
  ;; Lead
  (def mpp (midi-poly-player (partial syntorial-synth
                                      :osc1-waveform   0
                                      :cutoff          0.8
                                      :master-volume  -3.0
                                      :attack          0.005
                                      :release         0.5
                                      )))
  ;; Pad
  (def mpp (midi-poly-player (partial syntorial-synth
                                      :osc1-waveform   1
                                      :osc1-pulsewidth 0.15
                                      :cutoff          0.5
                                      :master-volume   6.0
                                      :attack          2.5
                                      :release         1.5
                                      )))


  ;; Adding 2nd Oscillator
  (def mpp (midi-poly-player (partial syntorial-synth
                                      :osc1-waveform   0
                                      :osc2-waveform   0
                                      :osc2-semi       19
                                      :mix             0.5
                                      :cutoff          0.8
                                      :master-volume  -3.0
                                      :attack          0.005
                                      :release         0.5
                                      )))

  (midi-player-stop)
  (stop)

)
