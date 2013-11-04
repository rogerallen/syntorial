(ns syntorial.start
  (:use [overtone.live]))

;; handy enums
(def wave {:saw 0 :pulse 1})

;; The basic starting point synth
(defsynth syntorial-starter-synth
  [note            60   ; midi note value
   amp             1.0  ; amplitude/volume
   osc1-waveform   0    ; 0=saw, 1=pulse
   osc1-pulsewidth 0.50 ;
   cutoff          1.0  ; 0.0 - 1.0
   gate            1    ; note-on/off
   out-bus         0    ; output bus
   ]
  (let [note1-freq  (midicps note)
        osc1-saw    (lf-saw note1-freq)
        osc1-pulse  (pulse note1-freq osc1-pulsewidth)
        osc1        (select osc1-waveform [osc1-saw osc1-pulse])
        osc-stereo  [osc1 osc1]
        filt-stereo (lpf osc-stereo (lin-exp cutoff 0.0 1.0 20.0 20000.0))
        env1        (env-gen (asr 0.01 1.0 0.01) :gate gate :action FREE)
        ]
    (out out-bus (* env1 filt-stereo))))

;; ======================================================================
;; here is a playground to experiment
(comment

  ;; play a single note
  (def s (syntorial-starter-synth :osc1-waveform   (:pulse wave)
                                  :osc1-pulsewidth 0.25
                                  :cutoff          0.75))
  (ctl s :gate 0)

  ;; use midi keyboard to control
  (def mpp (midi-poly-player (partial syntorial-starter-synth
                                      :osc1-waveform   (:saw wave)
                                      :osc1-pulsewidth 0.5
                                      :cutoff          0.75)))
  ;; handy stop routines
  (midi-player-stop)
  (stop)

)
