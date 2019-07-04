/*
 * DisplayWindow
 * Copyright (c) 2018-2019 Federico Berti
 * Last modified: 18/06/19 15:42
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package omegadrive.ui;

import omegadrive.system.SystemProvider;
import omegadrive.util.FileLoader;
import omegadrive.util.VideoMode;

import java.awt.event.KeyAdapter;
import java.time.LocalDate;

public interface DisplayWindow {

    String APP_NAME = "Helios";
    String VERSION = FileLoader.loadVersionFromManifest();
    String FRAME_TITLE_HEAD = APP_NAME + " " + VERSION;


    void addKeyListener(KeyAdapter keyAdapter);

    void setTitle(String rom);

    void init();

    void renderScreen(int[][] data, String label, VideoMode videoMode);

    void renderScreenLinear(int[] data, String label, VideoMode videoMode);

    void resetScreen();

    void setFullScreen(boolean value);

    String getRegionOverride();

    void reloadSystem(SystemProvider systemProvider);

    default String getAboutString() {
        int year = LocalDate.now().getYear();
        String yrString = year == 2018 ? "2018" : "2018-" + year;
        String res = FRAME_TITLE_HEAD + "\nA Sega Megadrive (Genesis) emulator, written in Java";
        res += "\n\nCopyright " + yrString + ", Federico Berti";
        res += "\n\nSee CREDITS.TXT for more information";
        res += "\n\nReleased under GPL v.3.0 license.";
        return res;
    }

    DisplayWindow HEADLESS_INSTANCE = new DisplayWindow() {
        @Override
        public void addKeyListener(KeyAdapter keyAdapter) {

        }

        @Override
        public void setTitle(String rom) {

        }

        @Override
        public void init() {

        }

        @Override
        public void renderScreen(int[][] data, String label, VideoMode videoMode) {

        }

        @Override
        public void renderScreenLinear(int[] data, String label, VideoMode videoMode) {

        }

        @Override
        public void resetScreen() {

        }

        @Override
        public void setFullScreen(boolean value) {

        }

        @Override
        public String getRegionOverride() {
            return null;
        }

        @Override
        public void reloadSystem(SystemProvider systemProvider) {

        }
    };


}