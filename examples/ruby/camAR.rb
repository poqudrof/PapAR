# -*- coding: utf-8 -*-

require 'jruby_art'
require 'jruby_art/app'

Processing::App::SKETCH_PATH = __FILE__

Processing::App::load_library :PapARt, :javacv, :toxiclibscore

module Papartlib
  include_package 'fr.inria.papart.procam'
end

class Sketch < Processing::App


  attr_reader :camera_tracking, :display, :papart, :moon

  def settings
    size 200, 200, P3D
  end
  
  def setup 

    frameSizeX = 1280
    frameSizeY = 800

    @camera_x = 640
    @camera_y = 480

    @papart = Papartlib::Papart.seeThrough self

    @moon = Moon.new

    @papart.startTracking

  end

  def draw

  end 
end


class Moon < Papartlib::PaperScreen

  def setup
    setDrawingSize(297, 210);
    loadMarkerBoard($app.sketchPath("") + "/data/A3-small1.cfg", 297, 210);
  end

  def draw
    setLocation(0, 0, 0)
    pg = beginDraw2D
    pg.background 40, 200, 200
    pg.endDraw
  end
end

Sketch.new unless defined? $app
