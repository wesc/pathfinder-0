(ns pf-web.db)


(def lasers-subtree
  {:id "subtree-uuid-1"
   :title "Lasers Subtree"
   :author "Wes Chow"
   :email "wes@demo.com"
   :short-description "Lasers! They are pretty cool. This is how we make them."

   :tech
   {"uuid-lasers"
    {:id "uuid-lasers"
     :title "Lasers"
     :short-description "The term \"laser\" is an acronym for \"light amplification by stimulated emission of radiation,\" which pretty much describes what it happens to be."
     :long-description "The theory dates back to a paper by Albert Einstein in 1917 which offered a derivation of Planck's Law concerning stimulated emission of electromagnetic radiation. In 1928, the atomic physicist Rudolf Ladenburg confirmed the phenomena of stimulated emission and negative absorption. By the time of the first true laser, American and Soviet Russian scientists had built masers, amplifying microwave radiation rather than light radiation. So it wasn't long before various others were attempting to build \"optical masers,\" as commonly termed (\"laser\" was coined in 1959). The first functional laser was demonstrated in May 1960 when the Hughes Research Laboratories introduced laser technology capable of storing data on optical devices. Later that same year, the Iranian Ali Javan headed an international team that produced the first gas laser, utilizing helium and neon, capable of continuous operation in the infrared spectrum."
     :dependencies ["uuid-nuclear-fission"]
     :references []}

    "uuid-combined-arms"
    {:id "uuid-combined-arms"
     :title "Combined Arms"
     :short-description "Arms that are combined."
     :long-description "The thoughtful combination of different arms in the same army is a very old concept - many generals of the antiquity have tried to supply their archers with pike regiments for defense against cavalry charges, for example. But it takes the chaotic battlefield of modernity, with its artillery shells and air-dropped bombs exploding everywhere, and its tanks rolling through everything, for the concept of combined arms to get true traction."
     :dependencies []
     :references []}

    "uuid-advanced-ballistics"
    {:id "uuid-advanced-ballistics"
     :title "Advanced Ballistics"
     :short-description "Advanced ballistics go boom."
     :long-description "Once humans started shooting off rockets, the need for a new understanding of ballistics became clear. The flight of rockets, jets, missiles, spacecraft and such simply couldn't be covered by the current knowledge of internal, transition, external and terminal ballistics that had been accumulated by engineers to that time. When Goddard and von Braun began lighting up the heavens, something more than a gyroscope was required to get the missile to impact where desired. And issues such as escape velocity and orbital reentry began to be of some importance, especially to future cosmonauts and astronauts. Meanwhile, jet aircraft capable of flying at Mach at altitudes of 10-15,000 meters (33-49,000 feet) changed the dynamics of flight into that of ballistics."
     :dependencies []
     :references []}

    "uuid-nuclear-fission"
    {:id "uuid-nuclear-fission"
     :title "Nuclear Fission"
     :short-description "Fission that is nuclear."
     :long-description "In contrast to nuclear fission – where energy is generated by the division of a nucleus – nuclear fusion occurs when two or more atomic nuclei slam together hard enough to fuse, which also releases photons in quantity. Fusion reactions power the stars of the universe, giving off lots of light and heat.\n\n
During WW2, research to create a fission bomb subsumed research into nuclear fusion. But in 1946 AD a patent was awarded to two British researchers for a prototype fusion reactor based on the Z-pinch concept, whereby a magnetic field could be generated to contain plasma (akin to that in a star). Commencing the following year, two teams in Britain began a series of ever larger experiments to generate electricity via fusion; another Brit, James Tuck, working at Los Alamos in the United States, built a series of fusion reactors leading to the largest, known derisively as the “Perhapsatron.” As it turned out, the name was apt, for experiments revealed instabilities in all these designs such that fusion was never reached."
     :dependencies ["uuid-combined-arms", "uuid-advanced-ballistics"]
     :references []}

    "uuid-nuclear-warheads"
    {:id "uuid-nuclear-warheads"
     :title "Nukes"
     :short-description "Nuclear warheads, yikes!"
     :long-description "You stick a big nuclear bomb on a missle and shoot it around the world."
     :dependencies ["uuid-nuclear-fission", "uuid-advanced-ballistics"]
     :references []}
    }

   :comments
   {"comment-id-1"
    {:id "comment-id-1"
     :seq 1
     :tech-id "uuid-lasers"
     :text "a comment about lasers"}

    "a-comment-id-2"   
    {:id "a-comment-id-2"
     :seq 2
     :tech-id "uuid-lasers"
     :text "another comment about lasers, which are frickin awesome"}

    "comment-id-3"    
    {:id "comment-id-3"
     :seq 3
     :tech-id "uuid-nuclear-warheads"
     :text "not so cool, man"}
    }
   })

(def flying-car-subtree
  {:id "b05d2a50-2f1f-4d72-b007-d4e4e57526aa"
   :title "Flying Car Subtree"
   :author "Wes Chow"
   :email "wes@demo.com"
   :description "Flying cars! They're pretty cool. This is how we make them."

   :tech
   {"4c5205b0-0a68-475f-a7b5-bdcf2950eb55"
    {:id "4c5205b0-0a68-475f-a7b5-bdcf2950eb55"
     :title "Cars"
     :short-description "Cars are vehicles that move around on wheels."
     :long-description "A car (or automobile) is a wheeled motor vehicle used for transportation. Most definitions of cars say that they run primarily on roads, seat one to eight people, have four wheels, and mainly transport people rather than goods."
     :dependencies []
     :references []}

    "b52e1c63-1221-43fe-b9c6-07776f9e42d9"
    {:id "b52e1c63-1221-43fe-b9c6-07776f9e42d9"
     :title "Planes"
     :short-description "Planes are vehicles that fly through the air."
     :long-description "An airplane or aeroplane (informally plane) is a fixed-wing aircraft that is propelled forward by thrust from a jet engine, propeller, or rocket engine. Airplanes come in a variety of sizes, shapes, and wing configurations. The broad spectrum of uses for airplanes includes recreation, transportation of goods and people, military, and research. Worldwide, commercial aviation transports more than four billion passengers annually on airliners and transports more than 200 billion tonne-kilometers of cargo annually, which is less than 1% of the world's cargo movement. Most airplanes are flown by a pilot on board the aircraft, but some are designed to be remotely or computer-controlled such as drones."
     :dependencies []
     :references []}

    "bdafe7a4-0f2a-427d-b501-5779051850b0"
    {:id "bdafe7a4-0f2a-427d-b501-5779051850b0"
     :title "Flying Cars"
     :short-description "Flying cars are cars that fly."
     :long-description "A flying car or roadable aircraft is a type of vehicle which can function as both a personal car and an aircraft. As used here, this includes vehicles which drive as motorcycles when on the road. The term 'flying car' is also sometimes used to include hovercars."
     :dependencies ["4c5205b0-0a68-475f-a7b5-bdcf2950eb55" "b52e1c63-1221-43fe-b9c6-07776f9e42d9"]}}

   :comments
   {}
   })


(def hovercar-subtree
  {:id "19e564aa-78a5-499a-8349-9e087776322b"
   :title "Hovercar Subtree"
   :author "Wes Chow"
   :email "wes@demo.com"
   :description "Hovering cars! They're pretty cool. This is how we make them."

   :tech
   {"4c5205b0-0a68-475f-a7b5-bdcf2950eb55"
    {:id "4c5205b0-0a68-475f-a7b5-bdcf2950eb55"
     :title "Cars"
     :short-description "Cars are vehicles that move around on wheels."
     :long-description "A car (or automobile) is a wheeled motor vehicle used for transportation. Most definitions of cars say that they run primarily on roads, seat one to eight people, have four wheels, and mainly transport people rather than goods."
     :dependencies []
     :references []}

    "6a7c8ca9-1029-4a6a-96fd-ec35d032a480"
    {:id "6a7c8ca9-1029-4a6a-96fd-ec35d032a480"
     :title "Propellers"
     :short-description "Rotating blades that provide lift."
     :long-description "A propeller (colloquially often called a screw if on a ship or an airscrew if on an aircraft), is a device with a rotating hub and radiating blades that are set at a pitch to form a helical spiral, that, when rotated, exerts linear thrust upon a working fluid, such as water or air. Propellers are used to pump fluid through a pipe or duct, or to create thrust to propel a boat through water or an aircraft through air. The blades are specially shaped so that their rotational motion through the fluid causes a pressure difference between the two surfaces of the blade by Bernoulli's principle which exerts force on the fluid."
     :dependencies []
     :references []}

    "d2383458-e9f2-492e-b6e2-491181401cc2"
    {:id "d2383458-e9f2-492e-b6e2-491181401cc2"
     :title "Hovercars"
     :short-description "Cars that hover."
     :long-description "A hover car is a personal vehicle that flies at a constant altitude of up to few meters (some feet) above the ground and used for personal transportation in the same way a modern automobile is employed. It usually appears in works of science fiction."
     :dependencies ["4c5205b0-0a68-475f-a7b5-bdcf2950eb55" "6a7c8ca9-1029-4a6a-96fd-ec35d032a480"]
     :references []}
    }

   :comments
   {}
   })

(def default-subtree-catalog
  {(:id lasers-subtree) lasers-subtree
   (:id flying-car-subtree) flying-car-subtree
   (:id hovercar-subtree) hovercar-subtree})

(def default-db
  {:name "Pathfinder"

   :active-panel [:home-panel nil]

   :subtree-catalog default-subtree-catalog
   :subtree lasers-subtree

   :sel-tech "uuid-lasers"
   :alert-message nil
   :download-link nil})
