;; ok, I finally figure out how to use overtone.
;; first, install lein2
;; then, lein new my-music-project
;; cd my-music-project
;; add into project.clj: dependence overtone 0.9.1
;; lein deps
;; lein repl
;; pay attention to the port number

;; inside emacs, install emacs-live as the whole .emacs.d
;; TODO get this working with my emcas configuration
;; TODO the them and color of emacs-live is awesome!

;; M-x clojure-mode
;; M-x cider-connect ; then enter "localhost" and port number

;; add the following (ns ..) and evaluate using C-x C-e

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; the emacs way
;; actaully I just need to install clojure-mode and cider.
;; then, I just go into the music project created by "lein new" and added overtone dependencies.
;; Any file in the project is ok.
;; Then, M-x cider-jack-in, I'm good to evaluate the (use 'overtone.live) and do the staffs.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; note: if I connect my mac to an iMac as monitor through thunderbold cabel,
;; which also transmit audio, it will not work to start overtone
(ns cmj.overtone
    (:use [overtone.live]
          [overtone.inst.piano]
          ))

;; do not really need the namespace, just use the packages
;; in the document of clojure, it is mentioned that "use" is like "require",
;; but also refers to each lib's namespace.
;; and it is prefered to use ":use" in a "ns" macro than to use it directly, like the following.
;; But it works.
(use 'overtone.live)
(use 'overtone.inst.piano)

;; Then evaluate any following and you are good to go.

(piano)
(piano 72)
(piano 80)

(sampled-piano)


(definst foo [] (saw 220))
;; this show the built-in doc for "saw"
;; we can type it in terminal repl
;; however, it doesnot working in repl commandline inside emacs
;; instead, evaluate here will result in the doc displayed in the *repl* buffer.
(odoc saw)

(foo)               ; this will run the above
(kill 47)           ; 47 is got from the previous line, the playing id
(kill foo)          ; can also use the function name
(foo)               ; can start multiple phases


;; this shows I can provide a parameter to customize frequency each time
(definst bar [freq 220]
  (saw freq)
  )

;; to run it
(bar 220)
(bar 660)
(kill bar)                              ; kill all instance of "bar"

(stop)              ; stop all

(definst trem [freq 440 depth 10 rate 6 length 3]
  (* 0.3                                ; * 0.3 means volumn 30%
     (line:kr 0 1 length FREE)          ; stop after some seconds. FREE is common practive in such control, so I don't need to kill it.
     (saw (+ freq (* depth (sin-osc:kr rate))))))

;; rates: :ar and :kr
;; :ar is audio rate, the rate of my audio card
;; :kr is control rate, about 1/60 of :ar
;; so :kr is often used in control signal, rather than outputing audio.

(trem)

;; demo, examples
(demo (example dbrown :rand-walk))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; a whole song (live coding)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; define some instruments
(definst kick [freq 120 dur 0.3 width 0.5]
  (let [freq-env (* freq (env-gen (perc 0 (* 0.99 dur))))
        env (env-gen (perc 0.01 dur) 1 1 0 1 FREE)
        sqr (* (env-gen (perc 0 0.01)) (pulse (* 2 freq) width))
        src (sin-osc freq-env)
        drum (+ sqr (* env src))]
    (compander drum drum 0.2 1 0.1 0.01 0.01)))

;;(kick)

(definst c-hat [amp 0.8 t 0.04]
  (let [env (env-gen (perc 0.001 t) 1 1 0 1 FREE)
        noise (white-noise)
        sqr (* (env-gen (perc 0.01 0.04)) (pulse 880 0.2))
        filt (bpf (+ sqr noise) 9000 0.5)]
    (* amp env filt)))

;;(c-hat)

;; metronome: 节拍器
;; BPM: beats per minute
(def metro (metronome 128))

(metro) ; => current beat number
(metro 100) ; => timestamp of 100th beat

;; define player
(defn player [beat]
  (at (metro beat) (kick))
  (at (metro (+ 0.5 beat)) (c-hat))
  ;; #'player is sent, so we are passing the variable "player" intead of the current value of var.
  ;; this enable us to redefine while it is playing
  (apply-by (metro (inc beat)) #'player (inc beat) []))

(player (metro))                        ; play it

(metro-bpm metro 60)                   ; evaluate this will live change the freqency

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; My attempt to create a piano player
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn player [beat]
  (at (metro beat) (piano))
  (at (metro (+ 0.5 beat)) (piano 72))
  (apply-by (metro (inc beat)) #'player (inc beat) [])
  )

(stop)
