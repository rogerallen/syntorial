(ns syntorial.syntorial
  (:use [overtone.live]))

(defsynth syntorial-synth [note            60   ; midi note value
                           amp             1.0  ; amplitude/volume
                           osc1-waveform   0    ; 0=saw, 1=pulse
                           osc1-pulsewidth 0.50 ;
                           cutoff          1.0  ; 0.0 - 1.0
                           master-volume   0.0  ; volume in dB
                           gate            1    ; note-on/off
                           out-bus         0    ; output bus
                           ]
  (let [note-freq  (midicps note)
        osc1-saw   (lf-saw note-freq)
        osc1-pulse (pulse note-freq osc1-pulsewidth)
        osc1       (select osc1-waveform [osc1-saw osc1-pulse])
        filt       (lpf osc1 (lin-exp cutoff 0.0 1.0 20.0 20000.0))
        env1       (env-gen (asr 0.01 1.0 0.01) :gate gate :action FREE)
        master     (pow 10 (/ master-volume 20)) ; dB -> V
        ]
    (out out-bus (* master env1 [filt filt]))))

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

  (midi-player-stop)
  (stop)

)
