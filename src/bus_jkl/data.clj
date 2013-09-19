(ns bus-jkl.data)

(def data
  [{:number 27
    :title ["Kauppatori" "Mustalampi"]
    :valid ["03.06.2013" "08.08.2013"]
    :districts ["HEIKKILÄ" "KAUPPATORI" "KELTINMÄKI" "MUSTALAMPI"]
    :route [{:stop "Väinönkatu" :info "pysäkki 10"}
            {:stop "Yliopistonkatu"}
            {:stop "Kalevankatu"}
            {:stop "Vapaudenkatu" :info "pysäkki 7"}
            {:stop "Cygnaeuksenkatu"}
            {:stop "Voionmaankatu"}
            {:stop "Keskikatu"}
            {:stop "Rautpohjankatu"}
            ;; add the missing streets when ready
            {:stop "Keltinmäentie"}
            {:stop "Keltinmäki"}
            {:stop "Mäyrämäentie"}
            {:stop "Mustalammentie"}]
    :schedule [{:day ["ma" "ti" "ke" "to" "pe"]
                :buses
                [{:time "05:35"}
                 {:time "06:35"}
                 {:time "07:05"}{:time "07:35"}
                 {:time "08:05"}{:time "08:35"}
                 {:time "09:05"}{:time "09:35"}
                 {:time "10:05"}{:time "10:35"}
                 {:time "11:05"}{:time "11:35"}
                 {:time "12:05"}{:time "12:35"}
                 {:time "13:05"}{:time "13:35"}
                 {:time "14:05"}{:time "14:35"}
                 {:time "15:05"}{:time "15:35"}
                 {:time "16:05"}{:time "16:35"}
                 {:time "17:05"}{:time "17:35"}
                 {:time "18:05"}{:time "18:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "19:05"}{:time "19:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "20:05"}{:time "20:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "21:05"}{:time "21:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "22:05"}{:time "22:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "23:05" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"]}
                 {:time "24:05" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"]}
                 {:time "01:05" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"
                                       "perjantaisin"]}]}
               {:day ["la"]
                :buses
                [{:time "06:35" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"
                                       "poikkeaa keskussairaalalla"]}
                 {:time "07:35" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"
                                       "poikkeaa keskussairaalalla"]}
                 {:time "08:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "09:05"}{:time "09:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "10:05"}{:time "10:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "11:05"}{:time "11:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "12:05"}{:time "12:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "13:05"}{:time "13:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "14:05"}{:time "14:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "15:05"}{:time "15:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "16:05"}{:time "16:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "17:05"}{:time "17:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "18:05"}{:time "18:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "19:05"}{:time "19:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "20:05"}{:time "20:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "21:05"}{:time "21:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "22:05"}{:time "22:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "23:05" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"]}
                 {:time "24:05" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"]}
                 {:time "01:05" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"]}]}
               {:day ["su"]
                :buses
                [{:time "06:35" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"
                                       "poikkeaa keskussairaalalla"]}
                 {:time "07:35" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"
                                       "poikkeaa keskussairaalalla"]}
                 {:time "08:35" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"
                                       "poikkeaa keskussairaalalla"]}
                 {:time "09:35" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"
                                       "poikkeaa keskussairaalalla"]}
                 {:time "10:35" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"
                                       "poikkeaa keskussairaalalla"]}
                 {:time "11:05"}{:time "11:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "12:05"}{:time "12:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "13:05"}{:time "13:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "14:05"}{:time "14:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "15:05"}{:time "15:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "16:05"}{:time "16:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "17:05"}{:time "17:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "18:05"}{:time "18:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "19:05"}{:time "19:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "20:05"}{:time "20:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "21:05"}{:time "21:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "22:05"}{:time "22:35" :info ["ajetaan Keltinmäkeen"]}
                 {:time "23:05" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"]}
                 {:time "24:05" :info ["Rautpohjankadun, Keljon ja Myllyjärven kautta"]}]}]}])

(defn read-data []
  "Reads the data from JSON-file / db or whatever. Currently hard coded"
  data)
